package com.querymate.QueryMate.service;

import com.querymate.QueryMate.entity.Project;
import com.querymate.QueryMate.exception.ResourceNotFoundException;
import com.querymate.QueryMate.repo.ProjectRepository;
import com.querymate.QueryMate.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

@Service
public class SchemaService {

    private final ProjectRepository projectRepository;
    private final CryptoUtils cryptoUtils;

    @Autowired
    public SchemaService(ProjectRepository projectRepository, CryptoUtils cryptoUtils) {
        this.projectRepository = projectRepository;
        this.cryptoUtils = cryptoUtils;
    }

    public String getSchemaForProject(Long projectId) {
        // Step 1: Fetch project details
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // Step 2: Decrypt credentials
        String username = cryptoUtils.decrypt(project.getDbUsername());
        String password = cryptoUtils.decrypt(project.getDbPassword());

        // Step 3: Build JDBC URL
        String url = "jdbc:" + project.getDbType().toLowerCase() + "://" +
                project.getDbHost() + ":" + project.getDbPort() + "/" +
                project.getDbName();

        StringBuilder schemaBuilder = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(url, username, password);
             ResultSet tables = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {

            DatabaseMetaData metaData = conn.getMetaData();

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");

                // Fetch primary keys
                Set<String> primaryKeys = new HashSet<>();
                try (ResultSet pkResult = metaData.getPrimaryKeys(null, null, tableName)) {
                    while (pkResult.next()) {
                        primaryKeys.add(pkResult.getString("COLUMN_NAME"));
                    }
                }

                schemaBuilder.append("Table: ").append(tableName).append(" (\n");

                try (ResultSet columns = metaData.getColumns(null, null, tableName, "%")) {
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        String typeName = columns.getString("TYPE_NAME");
                        int size = columns.getInt("COLUMN_SIZE");
                        int nullable = columns.getInt("NULLABLE");

                        boolean isPK = primaryKeys.contains(columnName);
                        String nullability = (nullable == DatabaseMetaData.columnNullable) ? "NULL" : "NOT NULL";

                        schemaBuilder.append("  ")
                                .append(columnName)
                                .append(" ")
                                .append(typeName)
                                .append(size > 0 ? "(" + size + ")" : "")
                                .append(" ")
                                .append(nullability);

                        if (isPK) {
                            schemaBuilder.append(" PRIMARY KEY");
                        }

                        schemaBuilder.append(",\n");
                    }
                }

                // Remove trailing comma and newline
                int length = schemaBuilder.length();
                schemaBuilder.delete(length - 2, length); // remove last comma and newline

                schemaBuilder.append("\n)\n\n");
            }

        } catch (SQLException e) {
            return "⚠️ Failed to fetch schema: " + e.getMessage();
        }

        String schema = schemaBuilder.toString().trim();
        return schema.isBlank()
                ? "⚠️ No valid tables found in the connected database."
                : schema;
    }
}
