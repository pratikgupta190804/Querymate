package com.querymate.QueryMate.service;

import java.sql.Connection;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querymate.QueryMate.dto.SchemaInfo;
import com.querymate.QueryMate.entity.Project;

@Service
public class SchemaExtractionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaExtractionService.class);
    
    @Autowired
    private DatabaseConnectionService connectionService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Extracts complete database schema and returns as SchemaInfo object
     */
    public SchemaInfo extractSchema(Project project) throws SQLException {
        String dbType = project.getDbType().toLowerCase();
        
        switch (dbType) {
            case "mysql":
                return extractMySQLSchema(project);
            case "postgresql":
                return extractPostgreSQLSchema(project);
            case "sqlserver":
                return extractSQLServerSchema(project);
            case "mongodb":
                return extractMongoDBSchema(project);
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
    
    /**
     * Extracts schema from MySQL database
     */
    private SchemaInfo extractMySQLSchema(Project project) throws SQLException {
        SchemaInfo schemaInfo = new SchemaInfo();
        schemaInfo.setDatabaseType("MySQL");
        schemaInfo.setDatabaseName(project.getDbName());
        
        List<SchemaInfo.TableInfo> tables = new ArrayList<>();
        
        try (Connection conn = connectionService.createConnection(project)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String dbName = project.getDbName();
            
            // Get all tables
            ResultSet tablesRS = metaData.getTables(dbName, null, "%", new String[]{"TABLE"});
            
            while (tablesRS.next()) {
                String tableName = tablesRS.getString("TABLE_NAME");
                SchemaInfo.TableInfo tableInfo = new SchemaInfo.TableInfo();
                tableInfo.setTableName(tableName);
                
                // Get columns
                List<SchemaInfo.ColumnInfo> columns = new ArrayList<>();
                ResultSet columnsRS = metaData.getColumns(dbName, null, tableName, "%");
                
                while (columnsRS.next()) {
                    SchemaInfo.ColumnInfo columnInfo = new SchemaInfo.ColumnInfo();
                    columnInfo.setColumnName(columnsRS.getString("COLUMN_NAME"));
                    columnInfo.setDataType(columnsRS.getString("TYPE_NAME"));
                    columnInfo.setNullable(columnsRS.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    columnInfo.setDefaultValue(columnsRS.getString("COLUMN_DEF"));
                    columns.add(columnInfo);
                }
                columnsRS.close();
                tableInfo.setColumns(columns);
                
                // Get primary keys
                List<String> primaryKeys = new ArrayList<>();
                ResultSet pkRS = metaData.getPrimaryKeys(dbName, null, tableName);
                while (pkRS.next()) {
                    primaryKeys.add(pkRS.getString("COLUMN_NAME"));
                }
                pkRS.close();
                tableInfo.setPrimaryKeys(primaryKeys);
                
                // Get foreign keys
                List<SchemaInfo.ForeignKeyInfo> foreignKeys = new ArrayList<>();
                ResultSet fkRS = metaData.getImportedKeys(dbName, null, tableName);
                while (fkRS.next()) {
                    SchemaInfo.ForeignKeyInfo fkInfo = new SchemaInfo.ForeignKeyInfo();
                    fkInfo.setColumnName(fkRS.getString("FKCOLUMN_NAME"));
                    fkInfo.setReferencedTable(fkRS.getString("PKTABLE_NAME"));
                    fkInfo.setReferencedColumn(fkRS.getString("PKCOLUMN_NAME"));
                    foreignKeys.add(fkInfo);
                }
                fkRS.close();
                tableInfo.setForeignKeys(foreignKeys);
                
                tables.add(tableInfo);
            }
            tablesRS.close();
        }
        
        schemaInfo.setTables(tables);
        return schemaInfo;
    }
    
    /**
     * Extracts schema from PostgreSQL database
     */
    private SchemaInfo extractPostgreSQLSchema(Project project) throws SQLException {
        SchemaInfo schemaInfo = new SchemaInfo();
        schemaInfo.setDatabaseType("PostgreSQL");
        schemaInfo.setDatabaseName(project.getDbName());
        
        List<SchemaInfo.TableInfo> tables = new ArrayList<>();
        
        try (Connection conn = connectionService.createConnection(project)) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // Get all tables from public schema
            ResultSet tablesRS = metaData.getTables(null, "public", "%", new String[]{"TABLE"});
            
            while (tablesRS.next()) {
                String tableName = tablesRS.getString("TABLE_NAME");
                SchemaInfo.TableInfo tableInfo = new SchemaInfo.TableInfo();
                tableInfo.setTableName(tableName);
                
                // Get columns
                List<SchemaInfo.ColumnInfo> columns = new ArrayList<>();
                ResultSet columnsRS = metaData.getColumns(null, "public", tableName, "%");
                
                while (columnsRS.next()) {
                    SchemaInfo.ColumnInfo columnInfo = new SchemaInfo.ColumnInfo();
                    columnInfo.setColumnName(columnsRS.getString("COLUMN_NAME"));
                    columnInfo.setDataType(columnsRS.getString("TYPE_NAME"));
                    columnInfo.setNullable(columnsRS.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    columnInfo.setDefaultValue(columnsRS.getString("COLUMN_DEF"));
                    columns.add(columnInfo);
                }
                columnsRS.close();
                tableInfo.setColumns(columns);
                
                // Get primary keys
                List<String> primaryKeys = new ArrayList<>();
                ResultSet pkRS = metaData.getPrimaryKeys(null, "public", tableName);
                while (pkRS.next()) {
                    primaryKeys.add(pkRS.getString("COLUMN_NAME"));
                }
                pkRS.close();
                tableInfo.setPrimaryKeys(primaryKeys);
                
                // Get foreign keys
                List<SchemaInfo.ForeignKeyInfo> foreignKeys = new ArrayList<>();
                ResultSet fkRS = metaData.getImportedKeys(null, "public", tableName);
                while (fkRS.next()) {
                    SchemaInfo.ForeignKeyInfo fkInfo = new SchemaInfo.ForeignKeyInfo();
                    fkInfo.setColumnName(fkRS.getString("FKCOLUMN_NAME"));
                    fkInfo.setReferencedTable(fkRS.getString("PKTABLE_NAME"));
                    fkInfo.setReferencedColumn(fkRS.getString("PKCOLUMN_NAME"));
                    foreignKeys.add(fkInfo);
                }
                fkRS.close();
                tableInfo.setForeignKeys(foreignKeys);
                
                tables.add(tableInfo);
            }
            tablesRS.close();
        }
        
        schemaInfo.setTables(tables);
        return schemaInfo;
    }
    
    /**
     * Extracts schema from SQL Server database
     */
    private SchemaInfo extractSQLServerSchema(Project project) throws SQLException {
        SchemaInfo schemaInfo = new SchemaInfo();
        schemaInfo.setDatabaseType("SQL Server");
        schemaInfo.setDatabaseName(project.getDbName());
        
        List<SchemaInfo.TableInfo> tables = new ArrayList<>();
        
        try (Connection conn = connectionService.createConnection(project)) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // Get all tables from dbo schema
            ResultSet tablesRS = metaData.getTables(null, "dbo", "%", new String[]{"TABLE"});
            
            while (tablesRS.next()) {
                String tableName = tablesRS.getString("TABLE_NAME");
                SchemaInfo.TableInfo tableInfo = new SchemaInfo.TableInfo();
                tableInfo.setTableName(tableName);
                
                // Get columns
                List<SchemaInfo.ColumnInfo> columns = new ArrayList<>();
                ResultSet columnsRS = metaData.getColumns(null, "dbo", tableName, "%");
                
                while (columnsRS.next()) {
                    SchemaInfo.ColumnInfo columnInfo = new SchemaInfo.ColumnInfo();
                    columnInfo.setColumnName(columnsRS.getString("COLUMN_NAME"));
                    columnInfo.setDataType(columnsRS.getString("TYPE_NAME"));
                    columnInfo.setNullable(columnsRS.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    columnInfo.setDefaultValue(columnsRS.getString("COLUMN_DEF"));
                    columns.add(columnInfo);
                }
                columnsRS.close();
                tableInfo.setColumns(columns);
                
                // Get primary keys
                List<String> primaryKeys = new ArrayList<>();
                ResultSet pkRS = metaData.getPrimaryKeys(null, "dbo", tableName);
                while (pkRS.next()) {
                    primaryKeys.add(pkRS.getString("COLUMN_NAME"));
                }
                pkRS.close();
                tableInfo.setPrimaryKeys(primaryKeys);
                
                // Get foreign keys
                List<SchemaInfo.ForeignKeyInfo> foreignKeys = new ArrayList<>();
                ResultSet fkRS = metaData.getImportedKeys(null, "dbo", tableName);
                while (fkRS.next()) {
                    SchemaInfo.ForeignKeyInfo fkInfo = new SchemaInfo.ForeignKeyInfo();
                    fkInfo.setColumnName(fkRS.getString("FKCOLUMN_NAME"));
                    fkInfo.setReferencedTable(fkRS.getString("PKTABLE_NAME"));
                    fkInfo.setReferencedColumn(fkRS.getString("PKCOLUMN_NAME"));
                    foreignKeys.add(fkInfo);
                }
                fkRS.close();
                tableInfo.setForeignKeys(foreignKeys);
                
                tables.add(tableInfo);
            }
            tablesRS.close();
        }
        
        schemaInfo.setTables(tables);
        return schemaInfo;
    }
    
    /**
     * Extracts schema from MongoDB database
     * Note: MongoDB is schemaless, so we'll provide a simplified structure
     */
    private SchemaInfo extractMongoDBSchema(Project project) {
        SchemaInfo schemaInfo = new SchemaInfo();
        schemaInfo.setDatabaseType("MongoDB");
        schemaInfo.setDatabaseName(project.getDbName());
        
        // MongoDB schema extraction requires MongoDB driver
        // This is a placeholder - actual implementation would use MongoDB driver
        List<SchemaInfo.TableInfo> tables = new ArrayList<>();
        schemaInfo.setTables(tables);
        
        logger.warn("MongoDB schema extraction not fully implemented yet");
        return schemaInfo;
    }
    
    /**
     * Converts SchemaInfo to JSON string for storage
     */
    public String schemaToJson(SchemaInfo schemaInfo) {
        try {
            return objectMapper.writeValueAsString(schemaInfo);
        } catch (Exception e) {
            logger.error("Error converting schema to JSON: {}", e.getMessage());
            return "{}";
        }
    }
    
    /**
     * Converts JSON string back to SchemaInfo
     */
    public SchemaInfo jsonToSchema(String json) {
        try {
            return objectMapper.readValue(json, SchemaInfo.class);
        } catch (Exception e) {
            logger.error("Error parsing schema JSON: {}", e.getMessage());
            return new SchemaInfo();
        }
    }
    
    /**
     * Formats schema as readable text for AI prompts
     */
    public String formatSchemaForPrompt(SchemaInfo schemaInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Database: ").append(schemaInfo.getDatabaseName())
          .append(" (").append(schemaInfo.getDatabaseType()).append(")\n\n");
        
        for (SchemaInfo.TableInfo table : schemaInfo.getTables()) {
            sb.append("Table: ").append(table.getTableName()).append("\n");
            sb.append("Columns:\n");
            
            for (SchemaInfo.ColumnInfo column : table.getColumns()) {
                sb.append("  - ").append(column.getColumnName())
                  .append(" (").append(column.getDataType()).append(")");
                
                if (!column.isNullable()) {
                    sb.append(" NOT NULL");
                }
                if (table.getPrimaryKeys().contains(column.getColumnName())) {
                    sb.append(" PRIMARY KEY");
                }
                sb.append("\n");
            }
            
            if (!table.getForeignKeys().isEmpty()) {
                sb.append("Foreign Keys:\n");
                for (SchemaInfo.ForeignKeyInfo fk : table.getForeignKeys()) {
                    sb.append("  - ").append(fk.getColumnName())
                      .append(" → ").append(fk.getReferencedTable())
                      .append(".").append(fk.getReferencedColumn()).append("\n");
                }
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
}
