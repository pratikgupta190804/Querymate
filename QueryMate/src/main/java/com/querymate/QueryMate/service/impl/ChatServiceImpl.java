package com.querymate.QueryMate.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querymate.QueryMate.dto.ChatMessageDto;
import com.querymate.QueryMate.dto.QueryIntent;
import com.querymate.QueryMate.entity.ChatMessage;
import com.querymate.QueryMate.entity.Project;
import com.querymate.QueryMate.exception.ResourceNotFoundException;
import com.querymate.QueryMate.repo.ChatMessageRepository;
import com.querymate.QueryMate.repo.ProjectRepository;
import com.querymate.QueryMate.service.ChatService;
import com.querymate.QueryMate.service.DynamicQueryExecutorService;
import com.querymate.QueryMate.service.IntentClassifierService;
import com.querymate.QueryMate.service.IntentValidatorService;
import com.querymate.QueryMate.service.SqlBuilderService;

@Service
public class ChatServiceImpl implements ChatService {

    private static final List<String> DISALLOWED_KEYWORDS = List.of("delete", "drop", "update", "truncate", "alter");

    private final ChatMessageRepository chatMessageRepository;
    private final ProjectRepository projectRepository;
    private final IntentClassifierService intentClassifierService;
    private final IntentValidatorService intentValidatorService;
    private final SqlBuilderService sqlBuilderService;
    private final DynamicQueryExecutorService queryExecutorService;

    @Autowired
    public ChatServiceImpl(
            ChatMessageRepository chatMessageRepository,
            ProjectRepository projectRepository,
            IntentClassifierService intentClassifierService,
            IntentValidatorService intentValidatorService,
            SqlBuilderService sqlBuilderService,
            DynamicQueryExecutorService queryExecutorService) {
        this.chatMessageRepository = chatMessageRepository;
        this.projectRepository = projectRepository;
        this.intentClassifierService = intentClassifierService;
        this.intentValidatorService = intentValidatorService;
        this.sqlBuilderService = sqlBuilderService;
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

        // 3️⃣ NEW ARCHITECTURE: Intent Classification (AI returns JSON only)
        QueryIntent intent;
        try {
            intent = intentClassifierService.classifyIntent(userInput, schema);
        } catch (Exception e) {
            String error = "❌ Failed to understand query intent: " + e.getMessage();
            ChatMessageDto errorMessage = saveMessage(projectId, "system", "result", error);
            return List.of(userMessage, errorMessage);
        }

        // 4️⃣ Validate Intent (Check if tables/columns exist)
        QueryIntent validatedIntent = intentValidatorService.validate(intent, schema);
        if (!validatedIntent.isPossible()) {
            ChatMessageDto errorMessage = saveMessage(projectId, "system", "result", 
                "❌ Invalid query: " + validatedIntent.getReason());
            return List.of(userMessage, errorMessage);
        }

        // 5️⃣ Build SQL (Deterministic code, no AI hallucination)
        String sqlQuery;
        try {
            sqlQuery = sqlBuilderService.buildSQL(validatedIntent);
        } catch (Exception e) {
            String error = "❌ Failed to build SQL: " + e.getMessage();
            ChatMessageDto errorMessage = saveMessage(projectId, "system", "result", error);
            return List.of(userMessage, errorMessage);
        }

        // Save generated SQL for user to see
        ChatMessageDto sqlMessage = saveMessage(projectId, "system", "sql", sqlQuery);

        // 6️⃣ Execute the SQL query
        String resultJson;
        try {
            List<Map<String, Object>> result = queryExecutorService.executeSQL(project, sqlQuery);
            resultJson = result.toString(); // optionally: convert to pretty JSON
        } catch (Exception e) {
            resultJson = "{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}";
        }

        // 7️⃣ Save execution result
        ChatMessageDto resultMessage = saveMessage(projectId, "system", "result", resultJson);

        return List.of(userMessage, sqlMessage, resultMessage);
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
