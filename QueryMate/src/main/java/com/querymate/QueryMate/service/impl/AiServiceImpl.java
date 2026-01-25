package com.querymate.QueryMate.service.impl;

import com.querymate.QueryMate.service.AiService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiServiceImpl implements AiService {

    private final ChatModel chatModel;

    @Autowired
    public AiServiceImpl(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String generateSQL(String userMessage, String schemaText, String dbType) {
        try {
            // Extract table names from schema for clear display
            String tableList = extractTableNames(schemaText);
            
            // Ultra-strict prompt with explicit examples
            String promptText = String.format(
    """
    DATABASE TABLES (THESE ARE THE ONLY TABLES THAT EXIST):
    %s

    FULL SCHEMA:
    %s

    VERY IMPORTANT RULES (MUST FOLLOW):
    1. You are ONLY allowed to use the tables and columns listed above.
    2. DO NOT assume or invent any table or column.
    3. If the user's question CANNOT be answered using the given schema,
       respond EXACTLY with:
       IMPOSSIBLE_QUERY
    4. DO NOT explain anything.
    5. DO NOT add comments.
    6. Output ONLY a SQL SELECT query OR IMPOSSIBLE_QUERY.
    7. Use JOIN only if both tables exist.

    User Question:
    %s
    """,
    tableList,
    schemaText,
    userMessage
);


            // Create message and prompt with temperature 0 for maximum accuracy
            Message message = new UserMessage(promptText);
            Prompt prompt = new Prompt(List.of(message), 
                OllamaOptions.builder()
                    .withTemperature(0.0)
                    .withTopP(0.8)
                    .withTopK(10)
                    .build()
            );

            // Generate response using Spring AI
            String content = chatModel.call(prompt).getResult().getOutput().getContent();

            // Clean and extract SQL query
            String sql = extractSQL(content);
            return sql;

        } catch (Exception e) {
            return "⚠️ Error generating SQL: " + e.getMessage();
        }
    }

    /**
     * Extract table names from schema text
     */
    private String extractTableNames(String schema) {
        StringBuilder tables = new StringBuilder();
        String[] lines = schema.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("Table:")) {
                String tableName = line.substring(line.indexOf("Table:") + 6, line.indexOf("(")).trim();
                tables.append("- ").append(tableName).append("\n");
            }
        }
        return tables.length() > 0 ? tables.toString() : "No tables found";
    }

    /**
     * Extract clean SQL query from AI response
     */
    private String extractSQL(String content) {
        // Method 1: Try to extract from ```sql markdown block
        if (content.contains("```sql")) {
            int start = content.indexOf("```sql") + 6;
            int end = content.indexOf("```", start);
            if (end > start) {
                String extracted = content.substring(start, end).trim();
                // Clean up any leading/trailing text
                if (extracted.toUpperCase().contains("SELECT")) {
                    int selectIndex = extracted.toUpperCase().indexOf("SELECT");
                    extracted = extracted.substring(selectIndex).trim();
                }
                return cleanSQL(extracted);
            }
        }
        
        // Method 2: Look for SELECT statement directly
        if (content.toUpperCase().contains("SELECT")) {
            int selectIndex = content.toUpperCase().indexOf("SELECT");
            String sqlPart = content.substring(selectIndex).trim();
            return cleanSQL(sqlPart);
        }

        // Fallback: return cleaned content
        return cleanSQL(content);
    }

    /**
     * Clean SQL query by removing extra text and formatting
     */
    private String cleanSQL(String sql) {
        // Remove everything after semicolon (including trailing text)
        if (sql.contains(";")) {
            int semiIndex = sql.indexOf(";");
            sql = sql.substring(0, semiIndex + 1);
        }
        
        // Remove common trailing phrases
        sql = sql.replaceAll("(?i)\\s+(here is|this is|example|using).*$", "");
        
        // Clean up whitespace
        sql = sql.trim();
        
        // Ensure it ends with semicolon
        if (!sql.endsWith(";")) {
            sql += ";";
        }
        
        return sql;
    }
}
