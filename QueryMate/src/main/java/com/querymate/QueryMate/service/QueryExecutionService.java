package com.querymate.QueryMate.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.querymate.QueryMate.entity.Project;

@Service
public class QueryExecutionService {
    
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionService.class);
    
    @Autowired
    private DatabaseConnectionService connectionService;
    
    @Value("${query.max-rows:1000}")
    private int maxRows;
    
    @Value("${query.timeout-seconds:30}")
    private int timeoutSeconds;
    
    @Value("${query.allow-modifications:true}")
    private boolean allowModifications;
    
    /**
     * Executes SQL query and returns results
     */
    public Map<String, Object> executeQuery(String sql, Project project) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Check if query is allowed
            if (!allowModifications && isModificationQuery(sql)) {
                result.put("success", false);
                result.put("error", "Modification queries (INSERT, UPDATE, DELETE, DROP) are not allowed");
                return result;
            }
            
            Connection connection = connectionService.createConnection(project);
            
            if (isSelectQuery(sql)) {
                return executeSelectQuery(sql, connection);
            } else {
                return executeModificationQuery(sql, connection);
            }
            
        } catch (SQLException e) {
            logger.error("SQL execution error: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("sqlState", e.getSQLState());
            return result;
        } catch (Exception e) {
            logger.error("Query execution error: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * Executes SELECT query
     */
    private Map<String, Object> executeSelectQuery(String sql, Connection connection) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        Statement statement = null;
        ResultSet rs = null;
        
        try {
            statement = connection.createStatement();
            statement.setQueryTimeout(timeoutSeconds);
            statement.setMaxRows(maxRows);
            
            long startTime = System.currentTimeMillis();
            rs = statement.executeQuery(sql);
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Get column metadata
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columns.add(metaData.getColumnName(i));
            }
            
            // Get rows
            List<Map<String, Object>> rows = new ArrayList<>();
            int rowCount = 0;
            
            while (rs.next() && rowCount < maxRows) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                rows.add(row);
                rowCount++;
            }
            
            result.put("success", true);
            result.put("columns", columns);
            result.put("rows", rows);
            result.put("rowCount", rowCount);
            result.put("executionTimeMs", executionTime);
            result.put("truncated", rowCount >= maxRows);
            
            logger.info("Query executed successfully. Rows: {}, Time: {}ms", rowCount, executionTime);
            
        } finally {
            closeResources(rs, statement, connection);
        }
        
        return result;
    }
    
    /**
     * Executes modification query (INSERT, UPDATE, DELETE)
     */
    private Map<String, Object> executeModificationQuery(String sql, Connection connection) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        Statement statement = null;
        
        try {
            statement = connection.createStatement();
            statement.setQueryTimeout(timeoutSeconds);
            
            long startTime = System.currentTimeMillis();
            int affectedRows = statement.executeUpdate(sql);
            long executionTime = System.currentTimeMillis() - startTime;
            
            result.put("success", true);
            result.put("affectedRows", affectedRows);
            result.put("executionTimeMs", executionTime);
            result.put("message", affectedRows + " row(s) affected");
            
            logger.info("Modification query executed. Affected rows: {}, Time: {}ms", affectedRows, executionTime);
            
        } finally {
            closeResources(null, statement, connection);
        }
        
        return result;
    }
    
    /**
     * Checks if query is a SELECT query
     */
    private boolean isSelectQuery(String sql) {
        String trimmedSql = sql.trim().toUpperCase();
        return trimmedSql.startsWith("SELECT") || 
               trimmedSql.startsWith("SHOW") || 
               trimmedSql.startsWith("DESCRIBE") ||
               trimmedSql.startsWith("EXPLAIN");
    }
    
    /**
     * Checks if query is a modification query
     */
    private boolean isModificationQuery(String sql) {
        String trimmedSql = sql.trim().toUpperCase();
        return trimmedSql.startsWith("INSERT") || 
               trimmedSql.startsWith("UPDATE") || 
               trimmedSql.startsWith("DELETE") ||
               trimmedSql.startsWith("DROP") ||
               trimmedSql.startsWith("CREATE") ||
               trimmedSql.startsWith("ALTER") ||
               trimmedSql.startsWith("TRUNCATE");
    }
    
    /**
     * Formats query results as readable text
     */
    public String formatResultsAsText(Map<String, Object> queryResult) {
        if (!(Boolean) queryResult.get("success")) {
            return "Error: " + queryResult.get("error");
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (queryResult.containsKey("rows")) {
            // SELECT query results
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) queryResult.get("rows");
            
            if (rows.isEmpty()) {
                sb.append("No results found.");
            } else {
                sb.append("Results (").append(rows.size()).append(" rows):\n\n");
                
                // Column headers
                @SuppressWarnings("unchecked")
                List<String> columns = (List<String>) queryResult.get("columns");
                sb.append(String.join(" | ", columns)).append("\n");
                sb.append("-".repeat(50)).append("\n");
                
                // Rows
                for (Map<String, Object> row : rows) {
                    List<String> values = new ArrayList<>();
                    for (String column : columns) {
                        Object value = row.get(column);
                        values.add(value != null ? value.toString() : "NULL");
                    }
                    sb.append(String.join(" | ", values)).append("\n");
                }
            }
            
            if ((Boolean) queryResult.getOrDefault("truncated", false)) {
                sb.append("\n(Results truncated to ").append(maxRows).append(" rows)");
            }
            
        } else if (queryResult.containsKey("affectedRows")) {
            // Modification query results
            sb.append(queryResult.get("message"));
        }
        
        sb.append("\n\nExecution time: ").append(queryResult.get("executionTimeMs")).append("ms");
        
        return sb.toString();
    }
    
    /**
     * Validates SQL syntax (basic check)
     */
    public boolean validateSQLSyntax(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        
        // Basic validation
        String trimmedSql = sql.trim();
        
        // Check for common SQL keywords
        String upperSql = trimmedSql.toUpperCase();
        boolean startsWithValidKeyword = 
            upperSql.startsWith("SELECT") ||
            upperSql.startsWith("INSERT") ||
            upperSql.startsWith("UPDATE") ||
            upperSql.startsWith("DELETE") ||
            upperSql.startsWith("CREATE") ||
            upperSql.startsWith("ALTER") ||
            upperSql.startsWith("DROP") ||
            upperSql.startsWith("SHOW") ||
            upperSql.startsWith("DESCRIBE") ||
            upperSql.startsWith("EXPLAIN");
        
        return startsWithValidKeyword;
    }
    
    /**
     * Safely closes database resources
     */
    private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("Error closing ResultSet: {}", e.getMessage());
            }
        }
        
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("Error closing Statement: {}", e.getMessage());
            }
        }
        
        if (conn != null) {
            connectionService.closeConnection(conn);
        }
    }
}
