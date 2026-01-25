package com.querymate.QueryMate.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.querymate.QueryMate.dto.ChatMessageDto;
import com.querymate.QueryMate.repo.ProjectRepository;
import com.querymate.QueryMate.service.AiService;
import com.querymate.QueryMate.service.ChatService;
import com.querymate.QueryMate.service.SchemaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/projects/{projectId}/chat")
@Tag(name = "Chat APIs", description = "Chat with your connected database using natural language queries.")
public class ChatController {

    private final ChatService chatService;
    private final AiService aiService;
    private final SchemaService schemaService;
    private final ProjectRepository projectRepository;

    @Autowired
    public ChatController(ChatService chatService,
                          AiService aiService,
                          SchemaService schemaService,
                          ProjectRepository projectRepository) {
        this.chatService = chatService;
        this.aiService = aiService;
        this.schemaService = schemaService;
        this.projectRepository = projectRepository;
    }

    /**
     * ✅ POST /api/projects/{projectId}/chat
     */
    @PostMapping
    @Operation(
            summary = "Ask question in natural language",
            description = "Submit a natural language message. The system generates a SQL query using Ollama Phi3, executes it on the connected database, and returns the result along with the full chat history."
    )
    public ResponseEntity<List<ChatMessageDto>> sendMessage(
            @PathVariable Long projectId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userMessage = body.get("content");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<ChatMessageDto> responseMessages = chatService.handleUserQuery(projectId, userMessage);
        return ResponseEntity.ok(responseMessages);
    }

    /**
     * ✅ GET /api/projects/{projectId}/chat
     */
    @GetMapping
    @Operation(
            summary = "Get chat history",
            description = "Returns all previous chat messages for the given project, including user questions, generated SQL, and database results."
    )
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(@PathVariable Long projectId) {
        return ResponseEntity.ok(chatService.getMessagesForProject(projectId));
    }
}
