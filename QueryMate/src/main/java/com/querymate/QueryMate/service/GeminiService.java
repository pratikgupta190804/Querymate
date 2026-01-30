package com.querymate.QueryMate.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.querymate.QueryMate.dto.SQLGenerationResponse;
import com.querymate.QueryMate.dto.SQLValidationResponse;
import com.querymate.QueryMate.dto.SchemaInfo;

@Service
public class GeminiService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.model:gemini-1.5-pro}")
    private String modelName;
    
    @Value("${gemini.project-id:querymate-ai}")
    private String projectId;
    
    @Value("${gemini.temperature:0.2}")
    private float temperature;
    
    /**
     * Layer 1: Generates SQL from natural language query with full schema context
     */
    public SQLGenerationResponse generateSQL(String userQuery, SchemaInfo schemaInfo, String dbType) {
        logger.info("Layer 1: Generating SQL for query: {}", userQuery);
        
        String prompt = buildSQLGenerationPrompt(userQuery, schemaInfo, dbType);
        String response = callGeminiAPI(prompt);
        
        return parseSQLGenerationResponse(response, dbType);
    }
    
    /**
     * Layer 2: Validates and refines the generated SQL
     */
    public SQLValidationResponse validateAndRefineSQL(String generatedSQL, String userQuery, 
                                                       SchemaInfo schemaInfo, String dbType) {
        logger.info("Layer 2: Validating SQL: {}", generatedSQL);
        
        String prompt = buildSQLValidationPrompt(generatedSQL, userQuery, schemaInfo, dbType);
        String response = callGeminiAPI(prompt);
        
        return parseSQLValidationResponse(response, generatedSQL);
    }
    
    /**
     * Builds prompt for Layer 1: SQL Generation
     */
    private String buildSQLGenerationPrompt(String userQuery, SchemaInfo schemaInfo, String dbType) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert SQL query generator. Generate a precise SQL query based on the user's question.\n\n");
        prompt.append("Database Type: ").append(dbType).append("\n\n");
        prompt.append("Database Schema:\n");
        prompt.append(formatSchemaForPrompt(schemaInfo));
        prompt.append("\n\n");
        
        // Database-specific syntax rules
        prompt.append(getDatabaseSyntaxRules(dbType));
        prompt.append("\n\n");
        
        prompt.append("User Question: ").append(userQuery).append("\n\n");
        
        prompt.append("IMPORTANT: Provide your response in the following format:\n");
        prompt.append("SQL: [your SQL query here]\n");
        prompt.append("EXPLANATION: [brief explanation of what the query does]\n");
        prompt.append("TABLES_USED: [comma-separated list of tables]\n");
        prompt.append("REASONING: [why you chose this approach]\n");
        
        return prompt.toString();
    }
    
    /**
     * Builds prompt for Layer 2: SQL Validation
     */
    private String buildSQLValidationPrompt(String generatedSQL, String userQuery, 
                                            SchemaInfo schemaInfo, String dbType) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert SQL validator. Review and validate the following SQL query.\n\n");
        prompt.append("Database Type: ").append(dbType).append("\n\n");
        prompt.append("Database Schema:\n");
        prompt.append(formatSchemaForPrompt(schemaInfo));
        prompt.append("\n\n");
        
        prompt.append("Original User Question: ").append(userQuery).append("\n\n");
        prompt.append("Generated SQL:\n").append(generatedSQL).append("\n\n");
        
        prompt.append("Validation Checklist:\n");
        prompt.append("1. Does the SQL use correct ").append(dbType).append(" syntax?\n");
        prompt.append("2. Are all table names and column names valid according to the schema?\n");
        prompt.append("3. Are JOINs correctly specified with proper foreign key relationships?\n");
        prompt.append("4. Are data types compatible in WHERE clauses and comparisons?\n");
        prompt.append("5. Is the query optimized and follows best practices?\n");
        prompt.append("6. Does it answer the user's original question?\n\n");
        
        prompt.append("IMPORTANT: Provide your response in the following format:\n");
        prompt.append("IS_VALID: [YES or NO]\n");
        prompt.append("VALIDATED_SQL: [corrected SQL if needed, or original if valid]\n");
        prompt.append("CONFIDENCE_SCORE: [0.0 to 1.0]\n");
        prompt.append("CORRECTIONS: [list any corrections made, or NONE]\n");
        prompt.append("WARNINGS: [list any warnings or suggestions]\n");
        prompt.append("RECOMMENDATION: [final recommendation]\n");
        
        return prompt.toString();
    }
    
    /**
     * Returns database-specific syntax rules
     */
    private String getDatabaseSyntaxRules(String dbType) {
        switch (dbType.toLowerCase()) {
            case "mysql":
                return "MySQL Syntax Rules:\n" +
                       "- Use backticks for identifiers: `table_name`, `column_name`\n" +
                       "- Use LIMIT clause for row limiting (not TOP)\n" +
                       "- Date functions: NOW(), CURDATE(), DATE_ADD(), etc.\n" +
                       "- String concatenation: CONCAT(str1, str2)\n" +
                       "- Auto-increment: AUTO_INCREMENT";
                       
            case "postgresql":
                return "PostgreSQL Syntax Rules:\n" +
                       "- Use double quotes for identifiers: \"table_name\", \"column_name\"\n" +
                       "- Use LIMIT and OFFSET for pagination\n" +
                       "- Date functions: CURRENT_TIMESTAMP, NOW(), CURRENT_DATE\n" +
                       "- String concatenation: str1 || str2\n" +
                       "- Auto-increment: SERIAL or IDENTITY";
                       
            case "sqlserver":
                return "SQL Server (T-SQL) Syntax Rules:\n" +
                       "- Use square brackets for identifiers: [table_name], [column_name]\n" +
                       "- Use TOP clause for row limiting (not LIMIT)\n" +
                       "- Date functions: GETDATE(), CURRENT_TIMESTAMP, DATEADD()\n" +
                       "- String concatenation: str1 + str2\n" +
                       "- Auto-increment: IDENTITY(1,1)";
                       
            default:
                return "Standard SQL Syntax";
        }
    }
    
    /**
     * Formats schema for prompt
     */
    private String formatSchemaForPrompt(SchemaInfo schemaInfo) {
        StringBuilder sb = new StringBuilder();
        
        for (SchemaInfo.TableInfo table : schemaInfo.getTables()) {
            sb.append("Table: ").append(table.getTableName()).append("\n");
            sb.append("  Columns:\n");
            
            for (SchemaInfo.ColumnInfo column : table.getColumns()) {
                sb.append("    - ").append(column.getColumnName())
                  .append(" (").append(column.getDataType()).append(")");
                
                if (!column.isNullable()) {
                    sb.append(" NOT NULL");
                }
                if (table.getPrimaryKeys() != null && table.getPrimaryKeys().contains(column.getColumnName())) {
                    sb.append(" PRIMARY KEY");
                }
                sb.append("\n");
            }
            
            if (table.getForeignKeys() != null && !table.getForeignKeys().isEmpty()) {
                sb.append("  Foreign Keys:\n");
                for (SchemaInfo.ForeignKeyInfo fk : table.getForeignKeys()) {
                    sb.append("    - ").append(fk.getColumnName())
                      .append(" → ").append(fk.getReferencedTable())
                      .append(".").append(fk.getReferencedColumn()).append("\n");
                }
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Calls Gemini API with the given prompt
     */
    private String callGeminiAPI(String prompt) {
        try {
            // Note: Using Google's Generative AI API (not Vertex AI for simplicity)
            // In production, you might want to use proper Vertex AI setup
            
            String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;
            
            // Build request body
            String requestBody = String.format(
                "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}],\"generationConfig\":{\"temperature\":%.1f,\"maxOutputTokens\":2048}}",
                prompt.replace("\"", "\\\"").replace("\n", "\\n"),
                temperature
            );
            
            // Make HTTP request
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            
            java.net.http.HttpResponse<String> response = client.send(request, 
                java.net.http.HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return extractTextFromResponse(response.body());
            } else {
                logger.error("Gemini API error: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("Gemini API call failed: " + response.statusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extracts text content from Gemini API response
     */
    private String extractTextFromResponse(String responseBody) {
        try {
            // Simple JSON parsing to extract text
            Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(responseBody);
            
            if (matcher.find()) {
                return matcher.group(1)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
            }
            
            return responseBody;
        } catch (Exception e) {
            logger.error("Error extracting text from response: {}", e.getMessage());
            return responseBody;
        }
    }
    
    /**
     * Parses Layer 1 response into SQLGenerationResponse
     */
    private SQLGenerationResponse parseSQLGenerationResponse(String response, String dbType) {
        SQLGenerationResponse result = new SQLGenerationResponse();
        
        try {
            String sql = extractValue(response, "SQL:");
            String explanation = extractValue(response, "EXPLANATION:");
            String tablesUsed = extractValue(response, "TABLES_USED:");
            String reasoning = extractValue(response, "REASONING:");
            
            result.setGeneratedSQL(sql != null ? sql.trim() : "");
            result.setExplanation(explanation != null ? explanation.trim() : "");
            result.setTablesUsed(tablesUsed != null ? Arrays.asList(tablesUsed.split(",\\s*")) : new ArrayList<>());
            result.setReasoning(reasoning != null ? reasoning.trim() : "");
            
            logger.info("Successfully generated SQL: {}", result.getGeneratedSQL());
            
        } catch (Exception e) {
            logger.error("Error parsing SQL generation response: {}", e.getMessage());
            result.setGeneratedSQL("");
            result.setExplanation("Error parsing response");
        }
        
        return result;
    }
    
    /**
     * Parses Layer 2 response into SQLValidationResponse
     */
    private SQLValidationResponse parseSQLValidationResponse(String response, String originalSQL) {
        SQLValidationResponse result = new SQLValidationResponse();
        
        try {
            String isValid = extractValue(response, "IS_VALID:");
            String validatedSQL = extractValue(response, "VALIDATED_SQL:");
            String confidenceStr = extractValue(response, "CONFIDENCE_SCORE:");
            String corrections = extractValue(response, "CORRECTIONS:");
            String warnings = extractValue(response, "WARNINGS:");
            String recommendation = extractValue(response, "RECOMMENDATION:");
            
            result.setValid(isValid != null && isValid.trim().equalsIgnoreCase("YES"));
            result.setValidatedSQL(validatedSQL != null ? validatedSQL.trim() : originalSQL);
            
            try {
                result.setConfidenceScore(confidenceStr != null ? Double.parseDouble(confidenceStr.trim()) : 0.8);
            } catch (NumberFormatException e) {
                result.setConfidenceScore(0.8);
            }
            
            result.setCorrections(corrections != null && !corrections.trim().equalsIgnoreCase("NONE") 
                ? Arrays.asList(corrections.split(",\\s*")) : new ArrayList<>());
            result.setWarnings(warnings != null ? Arrays.asList(warnings.split(",\\s*")) : new ArrayList<>());
            result.setRecommendation(recommendation != null ? recommendation.trim() : "Query validated");
            
            logger.info("SQL validation complete. Valid: {}, Confidence: {}", 
                result.isValid(), result.getConfidenceScore());
            
        } catch (Exception e) {
            logger.error("Error parsing SQL validation response: {}", e.getMessage());
            result.setValid(true);
            result.setValidatedSQL(originalSQL);
            result.setConfidenceScore(0.5);
        }
        
        return result;
    }
    
    /**
     * Extracts value after a label in the response
     */
    private String extractValue(String text, String label) {
        try {
            int startIndex = text.indexOf(label);
            if (startIndex == -1) return null;
            
            startIndex += label.length();
            int endIndex = text.indexOf("\n", startIndex);
            
            if (endIndex == -1) {
                return text.substring(startIndex).trim();
            }
            
            String value = text.substring(startIndex, endIndex).trim();
            
            // Check if next line starts with another label
            int nextLabelIndex = findNextLabel(text, endIndex);
            if (nextLabelIndex > endIndex) {
                value = text.substring(startIndex, nextLabelIndex).trim();
            }
            
            return value;
        } catch (Exception e) {
            logger.error("Error extracting value for label {}: {}", label, e.getMessage());
            return null;
        }
    }
    
    /**
     * Finds the next label in the text
     */
    private int findNextLabel(String text, int fromIndex) {
        String[] labels = {"SQL:", "EXPLANATION:", "TABLES_USED:", "REASONING:", 
                          "IS_VALID:", "VALIDATED_SQL:", "CONFIDENCE_SCORE:", 
                          "CORRECTIONS:", "WARNINGS:", "RECOMMENDATION:"};
        
        int minIndex = text.length();
        for (String label : labels) {
            int index = text.indexOf(label, fromIndex);
            if (index != -1 && index < minIndex) {
                minIndex = index;
            }
        }
        return minIndex;
    }
}
