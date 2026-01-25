package com.querymate.QueryMate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querymate.QueryMate.dto.QueryIntent;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IntentClassifierService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Autowired
    public IntentClassifierService(ChatModel chatModel) {
        this.chatModel = chatModel;
        this.objectMapper = new ObjectMapper();
    }

    public QueryIntent classifyIntent(String userQuestion, String schemaText) {
        try {
            String prompt = String.format("""
                You are an intent classifier. Analyze the user's question and the database schema, then return ONLY a JSON object.
                
                Database Schema:
                %s
                
                User Question: %s
                
                Return a JSON object with these fields:
                {
                  "operation": "SELECT",
                  "table": "table_name",
                  "columns": ["column1", "column2"] or ["*"],
                  "whereCondition": "column = 'value'" or null,
                  "orderBy": "column ASC/DESC" or null,
                  "limit": number or null,
                  "distinct": true or false,
                  "aggregateFunction": "COUNT/SUM/AVG" or null,
                  "possible": true or false,
                  "reason": "explanation if not possible"
                }
                
                Rules:
                - Use ONLY tables and columns from the schema
                - If the query is not possible with the given schema, set "possible": false
                - Return ONLY valid JSON, no other text
                """, schemaText, userQuestion);

            Message message = new UserMessage(prompt);
            Prompt aiPrompt = new Prompt(List.of(message),
                OllamaOptions.builder()
                    .withTemperature(0.0)
                    .build()
            );

            String response = chatModel.call(aiPrompt).getResult().getOutput().getContent();
            
            // Extract JSON from response
            String jsonContent = extractJSON(response);
            
            // Parse JSON to QueryIntent
            return objectMapper.readValue(jsonContent, QueryIntent.class);

        } catch (Exception e) {
            QueryIntent errorIntent = new QueryIntent();
            errorIntent.setPossible(false);
            errorIntent.setReason("Error classifying intent: " + e.getMessage());
            return errorIntent;
        }
    }

    private String extractJSON(String response) {
        // Remove markdown code blocks if present
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        }
        if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        
        // Find JSON object
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1).trim();
        }
        
        return response.trim();
    }
}
