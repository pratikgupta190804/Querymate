package com.querymate.QueryMate.dto;

import java.util.List;

public class SchemaInfo {
    
    private String databaseType;
    private String databaseName;
    private List<TableInfo> tables;
    
    public SchemaInfo() {}

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public List<TableInfo> getTables() {
        return tables;
    }

    public void setTables(List<TableInfo> tables) {
        this.tables = tables;
    }

    public static class TableInfo {
        private String tableName;
        private List<ColumnInfo> columns;
        private List<String> primaryKeys;
        private List<ForeignKeyInfo> foreignKeys;
        
        public TableInfo() {}

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public List<ColumnInfo> getColumns() {
            return columns;
        }

        public void setColumns(List<ColumnInfo> columns) {
            this.columns = columns;
        }

        public List<String> getPrimaryKeys() {
            return primaryKeys;
        }

        public void setPrimaryKeys(List<String> primaryKeys) {
            this.primaryKeys = primaryKeys;
        }

        public List<ForeignKeyInfo> getForeignKeys() {
            return foreignKeys;
        }

        public void setForeignKeys(List<ForeignKeyInfo> foreignKeys) {
            this.foreignKeys = foreignKeys;
        }
    }
    
    public static class ColumnInfo {
        private String columnName;
        private String dataType;
        private boolean nullable;
        private String defaultValue;
        
        public ColumnInfo() {}

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public boolean isNullable() {
            return nullable;
        }

        public void setNullable(boolean nullable) {
            this.nullable = nullable;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }
    
    public static class ForeignKeyInfo {
        private String columnName;
        private String referencedTable;
        private String referencedColumn;
        
        public ForeignKeyInfo() {}

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getReferencedTable() {
            return referencedTable;
        }

        public void setReferencedTable(String referencedTable) {
            this.referencedTable = referencedTable;
        }

        public String getReferencedColumn() {
            return referencedColumn;
        }

        public void setReferencedColumn(String referencedColumn) {
            this.referencedColumn = referencedColumn;
        }
    }
}
