package com.querymate.QueryMate.service.impl;

import com.querymate.QueryMate.dto.ChatMessageDto;
import com.querymate.QueryMate.entity.ChatMessage;
import com.querymate.QueryMate.entity.Project;
import com.querymate.QueryMate.exception.ResourceNotFoundException;
import com.querymate.QueryMate.repo.ChatMessageRepository;
import com.querymate.QueryMate.repo.ProjectRepository;
import com.querymate.QueryMate.service.ChatService;
import com.querymate.QueryMate.service.DynamicQueryExecutorService;
import com.querymate.QueryMate.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private static final List<String> DISALLOWED_KEYWORDS = List.of("delete", "drop", "update", "truncate", "alter");

    private final ChatMessageRepository chatMessageRepository;
    private final ProjectRepository projectRepository;
    private final OpenAIService openAIService;
    private final DynamicQueryExecutorService queryExecutorService;

    @Autowired
    public ChatServiceImpl(
            ChatMessageRepository chatMessageRepository,
            ProjectRepository projectRepository,
            OpenAIService openAIService,
            DynamicQueryExecutorService queryExecutorService
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.projectRepository = projectRepository;
        this.openAIService = openAIService;
        this.queryExecutorService = queryExecutorService;
    }

    private boolean containsDangerousCommand(String input) {
        String lower = input.toLowerCase();
        return DISALLOWED_KEYWORDS.stream().anyMatch(lower::contains);
    }


    @Override
    public ChatMessageDto saveMessage(Long projectId, String sender, String role, String content) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "ID", projectId));

        ChatMessage message = new ChatMessage(project, sender, role, content, LocalDateTime.now());
        ChatMessage saved = chatMessageRepository.save(message);
        return mapToDto(saved);
    }

    @Override
    public List<ChatMessageDto> getMessagesForProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "ID", projectId));

        List<ChatMessage> messages = chatMessageRepository.findByProjectOrderByTimestampAsc(project);
        return messages.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageDto> handleUserQuery(Long projectId, String userInput) {

        if (containsDangerousCommand(userInput)) {
            String warning = "❌ DELETE, UPDATE, DROP, and other write operations are not allowed. Please ask only for SELECT-based queries.";
            ChatMessageDto resultMessage = saveMessage(projectId, "system", "result", warning);
            return List.of(saveMessage(projectId, "user", "query", userInput), resultMessage);
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "ID", projectId));

        // 1️⃣ Save user message
        ChatMessageDto userMessage = saveMessage(projectId, "user", "query", userInput);

        // 2️⃣ Get schema from project
        String schema = project.getSchemaText();
        if (schema == null || schema.isBlank() || schema.contains("⚠️ No tables")) {
            String warning = "⚠️ No valid tables found in the connected database. Please check your DB connection or add some tables.";
            ChatMessageDto resultMessage = saveMessage(projectId, "system", "result", warning);
            return List.of(userMessage, resultMessage);
        }

        // 3️⃣ Generate SQL from OpenAI
        String fullResponse = openAIService.generateSQL(userInput, schema, project.getDbType());
        ChatMessageDto sqlMessage = saveMessage(projectId, "system", "sql", fullResponse);

        // 4️⃣ Extract raw SQL
        String sqlQuery = extractSQLFromCodeBlock(fullResponse);

        // 5️⃣ Execute the SQL query
        String resultJson;
        try {
            List<Map<String, Object>> result = queryExecutorService.executeSQL(project, sqlQuery);
            resultJson = result.toString(); // optionally: convert to pretty JSON
        } catch (Exception e) {
            resultJson = "{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}";
        }

        // 6️⃣ Save execution result
        ChatMessageDto resultMessage = saveMessage(projectId, "system", "result", resultJson);

        return List.of(userMessage, sqlMessage, resultMessage);
    }

    private String extractSQLFromCodeBlock(String content) {
        if (content.contains("```sql")) {
            int start = content.indexOf("```sql") + 6;
            int end = content.indexOf("```", start);
            if (end > start) {
                return content.substring(start, end).trim();
            }
        }
        return content.trim(); // fallback to raw content if not formatted
    }

    private ChatMessageDto mapToDto(ChatMessage message) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setMessageId(message.getMessageId());
        dto.setSender(message.getSender());
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setProjectId(message.getProject().getProjectId());
        return dto;
    }
}
