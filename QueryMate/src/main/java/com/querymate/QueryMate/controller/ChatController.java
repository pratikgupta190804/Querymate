package com.querymate.QueryMate.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.querymate.QueryMate.dto.ChatMessageDto;
import com.querymate.QueryMate.dto.ChatRequest;
import com.querymate.QueryMate.dto.SchemaInfo;
import com.querymate.QueryMate.entity.ChatMessage;
import com.querymate.QueryMate.service.ChatService;

@RestController
@RequestMapping("/api/projects")
public class ChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    @Autowired
    private ChatService chatService;
    
    /**
     * GET /projects/{projectId}/chat
     * Retrieves chat history for a project
     */
    @GetMapping("/{projectId}/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getChatHistory(@PathVariable Long projectId) {
        try {
            logger.info("GET /projects/{}/chat - Fetching chat history", projectId);
            List<ChatMessage> chatHistory = chatService.getChatHistory(projectId);
            // Convert entities to DTOs to avoid circular reference
            List<ChatMessageDto> dtos = chatHistory.stream()
                .map(msg -> new ChatMessageDto(
                    msg.getMessageId(),
                    msg.getSender(),
                    msg.getRole(),
                    msg.getContent(),
                    msg.getTimestamp()
                ))
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error fetching chat history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch chat history: " + e.getMessage()));
        }
    }
    
    /**
     * POST /projects/{projectId}/chat
     * Sends a message and processes it through the AI pipeline
     * Returns the newly created chat messages for the frontend
     */
    @PostMapping("/{projectId}/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long projectId,
            @RequestBody ChatRequest request) {
        try {
            logger.info("POST /projects/{}/chat - Processing query: {}", projectId, request.getContent());
            
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message content cannot be empty"));
            }
            
            // Process query and get the newly added messages
            List<ChatMessage> newMessages = chatService.processUserQueryAndReturnMessages(projectId, request.getContent());
            // Convert to DTOs
            List<ChatMessageDto> dtos = newMessages.stream()
                .map(msg -> new ChatMessageDto(
                    msg.getMessageId(),
                    msg.getSender(),
                    msg.getRole(),
                    msg.getContent(),
                    msg.getTimestamp()
                ))
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
            
        } catch (Exception e) {
            logger.error("Error processing chat message: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to process message: " + e.getMessage()));
        }
    }
    
    /**
     * DELETE /projects/{projectId}/chat
     * Clears chat history for a project
     */
    @DeleteMapping("/{projectId}/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> clearChatHistory(@PathVariable Long projectId) {
        try {
            logger.info("DELETE /projects/{}/chat - Clearing chat history", projectId);
            chatService.clearChatHistory(projectId);
            return ResponseEntity.ok(Map.of("message", "Chat history cleared successfully"));
        } catch (Exception e) {
            logger.error("Error clearing chat history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to clear chat history: " + e.getMessage()));
        }
    }
    
    /**
     * POST /projects/{projectId}/refresh-schema
     * Refreshes the database schema cache
     */
    @PostMapping("/{projectId}/refresh-schema")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> refreshSchema(@PathVariable Long projectId) {
        try {
            logger.info("POST /projects/{}/refresh-schema - Refreshing schema", projectId);
            SchemaInfo schemaInfo = chatService.refreshSchema(projectId);
            return ResponseEntity.ok(Map.of(
                "message", "Schema refreshed successfully",
                "schema", schemaInfo
            ));
        } catch (Exception e) {
            logger.error("Error refreshing schema: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to refresh schema: " + e.getMessage()));
        }
    }
    
    /**
     * GET /projects/{projectId}/schema
     * Retrieves the current database schema
     */
    @GetMapping("/{projectId}/schema")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSchema(@PathVariable Long projectId) {
        try {
            logger.info("GET /projects/{}/schema - Fetching schema", projectId);
            String schemaText = chatService.getSchemaAsText(projectId);
            return ResponseEntity.ok(Map.of("schema", schemaText));
        } catch (Exception e) {
            logger.error("Error fetching schema: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch schema: " + e.getMessage()));
        }
    }
    
    /**
     * POST /projects/{projectId}/test-connection
     * Tests the database connection
     */
    @PostMapping("/{projectId}/test-connection")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> testConnection(@PathVariable Long projectId) {
        try {
            logger.info("POST /projects/{}/test-connection - Testing connection", projectId);
            boolean isConnected = chatService.testConnection(projectId);
            
            if (isConnected) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Database connection successful"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                        "success", false,
                        "message", "Database connection failed"
                    ));
            }
        } catch (Exception e) {
            logger.error("Error testing connection: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Connection test failed: " + e.getMessage()
                ));
        }
    }
}
