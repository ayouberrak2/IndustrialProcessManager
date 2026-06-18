package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static DatabaseConnection instance;

    private final String url;
    private final String username;
    private final String password;

    private DatabaseConnection() throws SQLException {
        Properties properties = loadProperties();

        url = properties.getProperty("db.url");
        username = properties.getProperty("db.username");
        password = properties.getProperty("db.password");

        loadMysqlDriver();
    }

    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }

        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private Properties loadProperties() throws SQLException {
        Properties properties = new Properties();

        try (InputStream input = DatabaseConnection.class.getResourceAsStream("/database.properties")) {
            if (input == null) {
                throw new SQLException("fichier introuvable");
            }

            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new SQLException("error : ", exception);
        }
    }

    private void loadMysqlDriver() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("Driver MySQL introuvable", exception);
        }
    }

}
