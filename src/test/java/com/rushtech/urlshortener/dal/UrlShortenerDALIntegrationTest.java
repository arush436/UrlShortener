package com.rushtech.urlshortener.dal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UrlShortenerDALIntegrationTest {

    private static final String DATABASE_URL = "jdbc:sqlite:test_url_shortener_db.db";
    private static final String TEST_LONG_URL = "http://example.com";
    private static final String TEST_TOKEN = "testToken";

    private UrlShortenerDAL urlShortenerDAL;

    @BeforeEach
    void setUp() {
        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            createTablesInDatabase(conn);
            urlShortenerDAL = new UrlShortenerDAL(DATABASE_URL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTablesInDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS original_urls (id INTEGER PRIMARY KEY AUTOINCREMENT, long_url TEXT NOT NULL UNIQUE)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tokens (token TEXT PRIMARY KEY, original_url_id INTEGER NOT NULL, FOREIGN KEY (original_url_id) REFERENCES original_urls(id))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        try {
            dropTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void insertOriginalUrl_WithValidLongUrl_ShouldReturnGeneratedId() {
        long originalUrlId = urlShortenerDAL.insertOriginalUrl(TEST_LONG_URL);

        long retrievedOriginalUrlId = urlShortenerDAL.getOriginalUrlId(TEST_LONG_URL);

        assertEquals(originalUrlId, retrievedOriginalUrlId);
    }

    @Test
    void getOriginalUrl_WithValidToken_ShouldReturnOriginalUrl() {
        long originalUrlId = urlShortenerDAL.insertOriginalUrl(TEST_LONG_URL);

        urlShortenerDAL.insertUrlMapping(TEST_TOKEN, originalUrlId);

        String retrievedOriginalUrl = urlShortenerDAL.getOriginalUrl(TEST_TOKEN);

        assertEquals(TEST_LONG_URL, retrievedOriginalUrl);
    }

    @Test
    void getOriginalUrl_WithNonExistingToken_ShouldReturnNull() {
        String retrievedOriginalUrl = urlShortenerDAL.getOriginalUrl(TEST_TOKEN);

        assertNull(retrievedOriginalUrl);
    }

    private void dropTables() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS original_urls");
            stmt.executeUpdate("DROP TABLE IF EXISTS tokens");
        }
    }
}
