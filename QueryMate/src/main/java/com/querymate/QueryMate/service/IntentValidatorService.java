package com.querymate.QueryMate.service;

import com.querymate.QueryMate.dto.QueryIntent;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IntentValidatorService {

    public QueryIntent validate(QueryIntent intent, String schemaText) {
        // Extract available tables and columns from schema
        Map<String, Set<String>> schemaMap = parseSchema(schemaText);

        // Validate table exists
        if (intent.getTable() == null || !schemaMap.containsKey(intent.getTable().toLowerCase())) {
            intent.setPossible(false);
            intent.setReason("Table '" + intent.getTable() + "' does not exist. Available tables: " + schemaMap.keySet());
            return intent;
        }

        Set<String> availableColumns = schemaMap.get(intent.getTable().toLowerCase());

        // Validate columns exist (if not *)
        if (intent.getColumns() != null && !intent.getColumns().contains("*")) {
            for (String column : intent.getColumns()) {
                if (!availableColumns.contains(column.toLowerCase())) {
                    intent.setPossible(false);
                    intent.setReason("Column '" + column + "' does not exist in table '" + intent.getTable() + 
                                   "'. Available columns: " + availableColumns);
                    return intent;
                }
            }
        }

        return intent;
    }

    private Map<String, Set<String>> parseSchema(String schemaText) {
        Map<String, Set<String>> schemaMap = new HashMap<>();
        String[] lines = schemaText.split("\n");
        String currentTable = null;

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Table:")) {
                // Extract table name
                int start = line.indexOf("Table:") + 6;
                int end = line.indexOf("(");
                if (end > start) {
                    currentTable = line.substring(start, end).trim().toLowerCase();
                    schemaMap.put(currentTable, new HashSet<>());
                }
            } else if (currentTable != null && !line.isEmpty() && !line.equals(")")) {
                // Extract column name
                String[] parts = line.split("\\s+");
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    String columnName = parts[0].toLowerCase();
                    schemaMap.get(currentTable).add(columnName);
                }
            }
        }

        return schemaMap;
    }
}
