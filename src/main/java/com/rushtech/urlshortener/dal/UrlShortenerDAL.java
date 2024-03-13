package com.rushtech.urlshortener.dal;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

public class UrlShortenerDAL implements IUrlShortenerDAL {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerDAL.class);

    private final HikariDataSource dataSource;
    private final int expiryDateMonthsInFuture;

    public UrlShortenerDAL(String databaseUrl, int maxPoolSize, int connectionTimeoutMilliSeconds, int expiryDateMonthsInFuture) {
        this.expiryDateMonthsInFuture = expiryDateMonthsInFuture;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        config.setMaximumPoolSize(maxPoolSize);
        config.setConnectionTimeout(connectionTimeoutMilliSeconds);

        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public String getOriginalUrl(String token) {
        String originalUrl = null;
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT ou.long_url " +
                            "FROM original_urls ou " +
                            "JOIN tokens t ON ou.id = t.original_url_id " +
                            "WHERE t.token = ?")
            ) {
                stmt.setString(1, token);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        originalUrl = rs.getString("long_url");
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            rollbackTransaction(conn);
            handleSQLException("Error executing SQL query", e);
        } finally {
            closeConnection(conn);
        }
        return originalUrl;
    }

    @Override
    public long getOriginalUrlId(String longUrl) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id FROM original_urls WHERE long_url = ?")
        ) {
            stmt.setString(1, longUrl);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong("id") : -1;
            }
        } catch (SQLException e) {
            handleSQLException("Error retrieving original URL ID", e);
            return -1;
        }
    }

    @Override
    public void updateToken(String token, long originalUrlId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE tokens SET token = ? WHERE original_url_id = ?")
        ) {
            stmt.setString(1, token);
            stmt.setLong(2, originalUrlId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            handleSQLException("Error updating token", e);
        }
    }

    @Override
    public long insertOriginalUrl(String longUrl) {
        LocalDateTime expirationDateTime = LocalDateTime.now().plusMonths(expiryDateMonthsInFuture);
        Timestamp expirationTimestamp = Timestamp.valueOf(expirationDateTime);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO original_urls (long_url, expiration_date) VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setString(1, longUrl);
            stmt.setTimestamp(2, expirationTimestamp);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    logger.error("Inserting original URL failed, no ID obtained.");
                    throw new SQLException("Inserting original URL failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            handleSQLException("Error inserting original URL into database", e);
        }
        return -1;
    }

    @Override
    public void insertUrlMapping(String token, long originalUrlId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO tokens (token, original_url_id) VALUES (?, ?)")
        ) {
            stmt.setString(1, token);
            stmt.setLong(2, originalUrlId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            handleSQLException("Error inserting new URL mapping into database", e);
        }
    }

    @Override
    public String getTokenForOriginalUrl(long originalUrlId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT token FROM tokens WHERE original_url_id = ?")
        ) {
            stmt.setLong(1, originalUrlId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("token") : null;
            }
        } catch (SQLException e) {
            handleSQLException("Error retrieving token for original URL", e);
            return null;
        }
    }

    @Override
    public boolean deleteShortUrl(String token) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            long originalUrlId = getOriginalUrlIdFromToken(conn, token);

            if (originalUrlId != -1) {
                boolean mappingDeleted = deleteMappingEntry(conn, token);
                boolean originalUrlDeleted = deleteOriginalUrlEntry(conn, originalUrlId);

                if (mappingDeleted && originalUrlDeleted) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            rollbackTransaction(conn);
            handleSQLException("Error deleting short URL and associated original URL from database", e);
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    private boolean deleteMappingEntry(Connection conn, String token) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM tokens WHERE token = ?")
        ) {
            stmt.setString(1, token);
            return stmt.executeUpdate() > 0;
        }
    }

    private boolean deleteOriginalUrlEntry(Connection conn, long originalUrlId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM original_urls WHERE id = ?")
        ) {
            stmt.setLong(1, originalUrlId);
            return stmt.executeUpdate() > 0;
        }
    }

    private long getOriginalUrlIdFromToken(Connection conn, String token) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT original_url_id FROM tokens WHERE token = ?")
        ) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong("original_url_id") : -1;
            }
        }
    }

    @Override
    public void incrementRedirectCount(String originalUrl) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO redirect_analytics (original_url, redirect_count) " +
                             "VALUES (?, COALESCE((SELECT redirect_count FROM redirect_analytics WHERE original_url = ?), 0) + 1)")
        ) {
            insertStmt.setString(1, originalUrl);
            insertStmt.setString(2, originalUrl);
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            handleSQLException("Error updating redirect count", e);
        }
    }

    private void rollbackTransaction(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Error rolling back transaction", e);
            }
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true); // Reset auto-commit mode
                connection.close();
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
    }

    private void handleSQLException(String message, SQLException e) {
        logger.error(message, e);
        throw new UrlShortenerDataAccessException(message, e);
    }
}
