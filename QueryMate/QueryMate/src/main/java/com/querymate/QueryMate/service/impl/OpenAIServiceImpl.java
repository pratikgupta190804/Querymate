package com.querymate.QueryMate.service.impl;

import com.querymate.QueryMate.dto.ChatMessageDto;
import com.querymate.QueryMate.service.OpenAIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAIServiceImpl implements OpenAIService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.model}")
    private String model;

    @Value("${openrouter.referer}")
    private String referer;

    private static final List<String> DISALLOWED_KEYWORDS = List.of("delete", "drop", "update", "truncate", "alter");

    private boolean containsDangerousCommand(String input) {
        String lower = input.toLowerCase();
        return DISALLOWED_KEYWORDS.stream().anyMatch(lower::contains);
    }

    @Override
    public String generateSQL(String userMessage, String schemaText, String dbType) {

        RestTemplate restTemplate = new RestTemplate();

        // Build structured prompt
        String prompt = """
        You are a highly accurate SQL assistant. Based on the given database schema and user request, generate a %s-compatible **single** SQL SELECT query.

        📌 Requirements:
        - ONLY use valid %s SQL syntax.
        - The schema describes all available tables and columns. Use only those.
        - Focus only on SELECT queries. Do NOT use INSERT, UPDATE, DELETE, DROP, or any DDL/DML.
        - If the request cannot be fulfilled, return an informative comment.

        🎯 Output format:
        Respond with ONLY the SQL code inside a markdown code block like this:
        ```sql
        SELECT ...
        ```

        🧱 Database Schema:
        %s

        📝 User Request:
        %s
        """.formatted(dbType.toUpperCase(), dbType.toUpperCase(), schemaText, userMessage);

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", referer);
        headers.set("X-Title", "QueryMate");

        // Prepare request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "⚠️ Failed to get a valid response from OpenRouter.";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices == null || choices.isEmpty()) {
                return "⚠️ No response from language model.";
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = message.get("content").toString().trim();

            // Extract SQL from markdown block
            if (content.contains("```sql")) {
                int start = content.indexOf("```sql") + 6;
                int end = content.indexOf("```", start);
                if (end > start) {
                    return content.substring(start, end).trim();
                }
            }

            return content; // fallback if markdown block is not found

        } catch (Exception e) {
            return "⚠️ Error generating SQL: " + e.getMessage();
        }
    }
}
