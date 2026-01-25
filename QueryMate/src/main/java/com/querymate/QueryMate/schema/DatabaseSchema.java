package com.querymate.QueryMate.schema;

import java.util.List;
import java.util.Map;

public class DatabaseSchema {

    private final List<String> tables;
    private final Map<String, List<String>> columns;

    public DatabaseSchema(List<String> tables, Map<String, List<String>> columns) {
        this.tables = tables;
        this.columns = columns;
    }

    public List<String> getTables() {
        return tables;
    }

    public Map<String, List<String>> getColumns() {
        return columns;
    }

    public boolean hasTable(String table) {
        return tables.contains(table);
    }

    public boolean hasColumn(String table, String column) {
        return columns.containsKey(table) && columns.get(table).contains(column);
    }
}
