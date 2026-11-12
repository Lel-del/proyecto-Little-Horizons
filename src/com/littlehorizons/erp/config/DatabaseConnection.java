package com.littlehorizons.erp.config;

import com.littlehorizons.erp.util.AppLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection manager for MySQL 8.0+ using JDBC.
 */
public final class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/erp_little_horizons?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String PROPERTIES_PATH = "config/db.properties";
    private static final String EXAMPLE_PROPERTIES_PATH = "config/db.properties.example";

    private static DatabaseConnection instance;

    private final String url;
    private final String user;
    private final String password;

    private DatabaseConnection() {
        Properties props = loadProperties();
        this.url = resolveConfig("LH_DB_URL", props.getProperty("db.url"), DEFAULT_URL);
        this.user = resolveConfig("LH_DB_USER", props.getProperty("db.user"), DEFAULT_USER);
        this.password = resolveConfig("LH_DB_PASSWORD", props.getProperty("db.password"), DEFAULT_PASSWORD);
        loadJdbcDriver();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void loadJdbcDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            AppLogger.getInstance().logError("DatabaseConnection", "MySQL JDBC Driver not found on classpath", ex);
        }
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        Path primary = Paths.get(PROPERTIES_PATH);
        Path fallback = Paths.get(EXAMPLE_PROPERTIES_PATH);
        Path selected = Files.exists(primary) ? primary : (Files.exists(fallback) ? fallback : null);

        if (selected != null) {
            try (InputStream in = new FileInputStream(selected.toFile())) {
                props.load(in);
            } catch (IOException ex) {
                AppLogger.getInstance().logWarning("DatabaseConnection",
                        "Could not read " + selected + ", using defaults/env variables: " + ex.getMessage());
            }
        }
        return props;
    }

    private String resolveConfig(String envKey, String propValue, String defaultValue) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        if (propValue != null) {
            return propValue.trim();
        }
        return defaultValue;
    }

    /**
     * Opens a fresh JDBC connection so callers can safely manage it with try-with-resources.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Verifies database connectivity without exposing credentials.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException ex) {
            AppLogger.getInstance().logError("DatabaseConnection.testConnection",
                    "Failed to connect to database URL: " + url, ex);
            return false;
        }
    }

    public String getUrl() {
        return url;
    }
}
