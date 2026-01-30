package com.querymate.QueryMate.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querymate.QueryMate.entity.Project;
import com.querymate.QueryMate.utils.CryptoUtils;

@Service
public class DatabaseConnectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionService.class);
    
    @Autowired
    private CryptoUtils cryptoUtils;
    
    /**
     * Creates a dynamic database connection based on project configuration
     * Supports both local and cloud databases
     */
    public Connection createConnection(Project project) throws SQLException {
        String connectionType = project.getConnectionType();
        
        if ("cloud".equalsIgnoreCase(connectionType)) {
            return createCloudConnection(project);
        } else {
            return createLocalConnection(project);
        }
    }
    
    /**
     * Creates connection to cloud database using connection string
     */
    private Connection createCloudConnection(Project project) throws SQLException {
        String connectionString = project.getCloudConnectionString();
        
        if (connectionString == null || connectionString.isEmpty()) {
            throw new SQLException("Cloud connection string is missing");
        }
        
        // Decrypt connection string if it's encrypted
        try {
            connectionString = cryptoUtils.decrypt(connectionString);
        } catch (Exception e) {
            logger.warn("Failed to decrypt connection string, using as-is: {}", e.getMessage());
        }
        
        // Decrypt username and password if available
        String username = project.getDbUsername();
        String password = project.getDbPassword();
        
        if (username != null && !username.isEmpty()) {
            try {
                username = cryptoUtils.decrypt(username);
            } catch (Exception e) {
                logger.warn("Failed to decrypt username, using as-is: {}", e.getMessage());
            }
        }
        
        if (password != null && !password.isEmpty()) {
            try {
                password = cryptoUtils.decrypt(password);
            } catch (Exception e) {
                logger.warn("Failed to decrypt password, using as-is: {}", e.getMessage());
            }
        }
        
        logger.info("Connecting to cloud database: {}", project.getCloudProvider());
        logger.debug("Connection URL: {}", connectionString);
        logger.debug("Username: {}", username);
        logger.debug("Password length: {}", password != null ? password.length() : 0);
        
        try {
            Connection connection;
            // If username/password are provided, use them for authentication
            if (username != null && !username.isEmpty() && password != null) {
                connection = DriverManager.getConnection(connectionString, username, password);
            } else {
                connection = DriverManager.getConnection(connectionString);
            }
            logger.info("Successfully connected to cloud database");
            return connection;
        } catch (SQLException e) {
            logger.error("Failed to connect to cloud database: {}", e.getMessage());
            throw new SQLException("Failed to connect to cloud database: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates connection to local database using individual parameters
     */
    private Connection createLocalConnection(Project project) throws SQLException {
        String dbType = project.getDbType();
        String host = project.getDbHost();
        Integer port = project.getDbPort();
        String dbName = project.getDbName();
        
        // Decrypt credentials before using them
        String username = project.getDbUsername();
        String password = project.getDbPassword();
        
        if (username != null && !username.isEmpty()) {
            try {
                username = cryptoUtils.decrypt(username);
            } catch (Exception e) {
                logger.warn("Failed to decrypt username, using as-is: {}", e.getMessage());
            }
        }
        
        if (password != null && !password.isEmpty()) {
            try {
                password = cryptoUtils.decrypt(password);
            } catch (Exception e) {
                logger.warn("Failed to decrypt password, using as-is: {}", e.getMessage());
            }
        }
        
        String jdbcUrl = buildJdbcUrl(dbType, host, port, dbName);
        
        logger.info("Connecting to local database: {} at {}", dbType, jdbcUrl);
        
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            logger.info("Successfully connected to local database");
            return connection;
        } catch (SQLException e) {
            logger.error("Failed to connect to local database: {}", e.getMessage());
            throw new SQLException("Failed to connect to local database: " + e.getMessage(), e);
        }
    }
    
    /**
     * Builds JDBC URL based on database type
     */
    private String buildJdbcUrl(String dbType, String host, Integer port, String dbName) {
        switch (dbType.toLowerCase()) {
            case "mysql":
                int mysqlPort = (port != null) ? port : 3306;
                return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", 
                    host, mysqlPort, dbName);
                
            case "postgresql":
                int pgPort = (port != null) ? port : 5432;
                return String.format("jdbc:postgresql://%s:%d/%s", host, pgPort, dbName);
                
            case "sqlserver":
                int sqlPort = (port != null) ? port : 1433;
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=true;trustServerCertificate=true", 
                    host, sqlPort, dbName);
                
            case "mongodb":
                int mongoPort = (port != null) ? port : 27017;
                return String.format("mongodb://%s:%d/%s", host, mongoPort, dbName);
                
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
    
    /**
     * Tests database connection
     */
    public boolean testConnection(Project project) {
        try (Connection connection = createConnection(project)) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            logger.error("Connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Safely closes database connection
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.debug("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing connection: {}", e.getMessage());
            }
        }
    }
}
