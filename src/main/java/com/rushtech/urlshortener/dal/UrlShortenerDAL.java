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
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
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
        } catch (SQLException e) {
            handleSQLException("Error executing SQL query", e);
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

    public long insertOriginalUrl(String longUrl) {
        // Check if the URL already exists
        String existingOriginalUrl = getExistingOriginalUrl(longUrl);
        if (existingOriginalUrl != null) {
            return -1; // Indicates that the URL already exists
        }

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
    public String getExistingOriginalUrl(String longUrl) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT long_url FROM original_urls WHERE long_url = ?")
        ) {
            stmt.setString(1, longUrl);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("long_url") : null;
            }
        } catch (SQLException e) {
            handleSQLException("Error checking existing original URL", e);
            return null;
        }
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
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM tokens WHERE token = ?")
        ) {
            stmt.setString(1, token);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            handleSQLException("Error deleting short URL from database", e);
            return false;
        }
    }

    private void handleSQLException(String message, SQLException e) {
        logger.error(message, e);
        throw new UrlShortenerDataAccessException(message, e);
    }
}
