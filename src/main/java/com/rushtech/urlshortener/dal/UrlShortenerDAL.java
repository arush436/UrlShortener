package com.rushtech.urlshortener.dal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

import java.sql.*;

public class UrlShortenerDAL implements IUrlShortenerDAL {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerDAL.class);

    private final SQLiteDataSource dataSource;

    public UrlShortenerDAL(String databaseUrl) {
        this.dataSource = new SQLiteDataSource();
        this.dataSource.setUrl(databaseUrl);
    }

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

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO original_urls (long_url) VALUES (?)",
                     Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setString(1, longUrl);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1); // Return the generated id
                } else {
                    logger.error("Inserting original URL failed, no ID obtained.");
                    throw new SQLException("Inserting original URL failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            handleSQLException("Error inserting original URL into database", e);
        }
        return -1; // Return a default value if insertion fails
    }

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

    private void handleSQLException(String message, SQLException e) {
        logger.error(message, e);
        throw new UrlShortenerDataAccessException(message, e);
    }
}
