package com.rushtech.urlshortener.dal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UrlShortenerDAL {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerDAL.class);

    private final SQLiteDataSource dataSource;

    public UrlShortenerDAL(String databaseUrl) {
        this.dataSource = new SQLiteDataSource();
        this.dataSource.setUrl(databaseUrl);
    }

    public String getOriginalUrl(String shortCode) {
        String originalUrl = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT ou.long_url " +
                             "FROM original_urls ou " +
                             "JOIN short_urls su ON ou.id = su.original_url_id " +
                             "WHERE su.short_code = ?");
        ) {
            stmt.setString(1, shortCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                originalUrl = rs.getString("long_url");
            }
        } catch (SQLException e) {
            logger.error("Error executing SQL query", e);
            throw new UrlShortenerDataAccessException("Error accessing data", e);
        }
        return originalUrl;
    }
}
