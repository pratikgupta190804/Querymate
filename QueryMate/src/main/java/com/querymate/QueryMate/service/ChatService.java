package com.querymate.QueryMate.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querymate.QueryMate.dto.ChatResponse;
import com.querymate.QueryMate.dto.SQLGenerationResponse;
import com.querymate.QueryMate.dto.SQLValidationResponse;
import com.querymate.QueryMate.dto.SchemaInfo;
import com.querymate.QueryMate.entity.ChatMessage;
import com.querymate.QueryMate.entity.Project;
import com.querymate.QueryMate.repo.ChatMessageRepository;
import com.querymate.QueryMate.repo.ProjectRepository;

@Service
public class ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private SchemaExtractionService schemaExtractionService;
    
    @Autowired
    private GeminiService geminiService;
    
    @Autowired
    private QueryExecutionService queryExecutionService;
    
    /**
     * Main orchestrator: Processes user query through the complete AI pipeline
     */
    @Transactional
    public ChatResponse processUserQuery(Long projectId, String userQuery) {
        logger.info("Processing user query for project {}: {}", projectId, userQuery);
        
        ChatResponse response = new ChatResponse();
        response.setUserQuery(userQuery);
        response.setTimestamp(LocalDateTime.now().toString());
        
        try {
            // Step 1: Load project
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));
            
            // Step 2: Save user message to history
            saveMessage(project, "user", "query", userQuery);
            
            // Step 3: Extract or load schema
            SchemaInfo schemaInfo = getSchemaInfo(project);
            
            if (schemaInfo.getTables() == null || schemaInfo.getTables().isEmpty()) {
                response.setErrors(List.of("Unable to extract database schema. Please check your database connection."));
                response.setSqlExecuted(false);
                saveMessage(project, "system", "error", "Schema extraction failed");
                return response;
            }
            
            String dbType = project.getDbType();
            
            // Step 4: Layer 1 - Generate SQL using Gemini
            logger.info("Layer 1: Generating SQL...");
            SQLGenerationResponse sqlGeneration = geminiService.generateSQL(userQuery, schemaInfo, dbType);
            response.setGeneratedSQL(sqlGeneration.getGeneratedSQL());
            response.setAiExplanation(sqlGeneration.getExplanation());
            
            saveMessage(project, "system", "sql-generated", sqlGeneration.getGeneratedSQL());
            
            // Step 5: Layer 2 - Validate and refine SQL
            logger.info("Layer 2: Validating SQL...");
            SQLValidationResponse validation = geminiService.validateAndRefineSQL(
                sqlGeneration.getGeneratedSQL(), 
                userQuery, 
                schemaInfo, 
                dbType
            );
            
            response.setValidatedSQL(validation.getValidatedSQL());
            response.setConfidenceScore(validation.getConfidenceScore());
            
            if (!validation.isValid()) {
                logger.warn("SQL validation failed. Using corrected SQL.");
                saveMessage(project, "system", "sql-corrected", 
                    "Original SQL had issues. Corrected version: " + validation.getValidatedSQL());
            }
            
            // Step 6: Execute validated SQL
            if (queryExecutionService.validateSQLSyntax(validation.getValidatedSQL())) {
                logger.info("Executing SQL query...");
                Map<String, Object> queryResult = queryExecutionService.executeQuery(
                    validation.getValidatedSQL(), 
                    project
                );
                
                response.setSqlExecuted(true);
                response.setQueryResults(queryResult);
                
                if ((Boolean) queryResult.get("success")) {
                    String formattedResults = queryExecutionService.formatResultsAsText(queryResult);
                    saveMessage(project, "system", "result", formattedResults);
                } else {
                    response.setErrors(List.of((String) queryResult.get("error")));
                    saveMessage(project, "system", "error", (String) queryResult.get("error"));
                }
            } else {
                response.setSqlExecuted(false);
                response.setErrors(List.of("Invalid SQL syntax"));
                saveMessage(project, "system", "error", "Invalid SQL syntax");
            }
            
            logger.info("Query processing completed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing query: {}", e.getMessage(), e);
            response.setSqlExecuted(false);
            response.setErrors(List.of("Error: " + e.getMessage()));
            
            try {
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project != null) {
                    saveMessage(project, "system", "error", e.getMessage());
                }
            } catch (Exception ex) {
                logger.error("Error saving error message: {}", ex.getMessage());
            }
        }
        
        return response;
    }
    
    /**
     * Processes user query and returns the newly created chat messages
     * This is used by the frontend which expects ChatMessage entities
     */
    @Transactional
    public List<ChatMessage> processUserQueryAndReturnMessages(Long projectId, String userQuery) {
        // Get the message count before processing
        int messageCountBefore = chatMessageRepository.findByProject_ProjectIdOrderByTimestampAsc(projectId).size();
        
        // Process the query (this saves messages internally)
        processUserQuery(projectId, userQuery);
        
        // Get all messages and return only the new ones
        List<ChatMessage> allMessages = chatMessageRepository.findByProject_ProjectIdOrderByTimestampAsc(projectId);
        
        // Return only the newly added messages
        if (allMessages.size() > messageCountBefore) {
            return allMessages.subList(messageCountBefore, allMessages.size());
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Retrieves chat history for a project
     */
    public List<ChatMessage> getChatHistory(Long projectId) {
        logger.info("Fetching chat history for project: {}", projectId);
        return chatMessageRepository.findByProject_ProjectIdOrderByTimestampAsc(projectId);
    }
    
    /**
     * Clears chat history for a project
     */
    @Transactional
    public void clearChatHistory(Long projectId) {
        logger.info("Clearing chat history for project: {}", projectId);
        chatMessageRepository.deleteByProject_ProjectId(projectId);
    }
    
    /**
     * Refreshes database schema for a project
     */
    @Transactional
    public SchemaInfo refreshSchema(Long projectId) throws Exception {
        logger.info("Refreshing schema for project: {}", projectId);
        
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));
        
        SchemaInfo schemaInfo = schemaExtractionService.extractSchema(project);
        String schemaJson = schemaExtractionService.schemaToJson(schemaInfo);
        
        project.setSchemaText(schemaJson);
        projectRepository.save(project);
        
        logger.info("Schema refreshed successfully for project: {}", projectId);
        return schemaInfo;
    }
    
    /**
     * Gets schema info, extracting if necessary
     */
    private SchemaInfo getSchemaInfo(Project project) {
        String schemaText = project.getSchemaText();
        
        // If schema is cached, use it
        if (schemaText != null && !schemaText.isEmpty() && !schemaText.equals("{}")) {
            logger.info("Using cached schema for project: {}", project.getProjectId());
            return schemaExtractionService.jsonToSchema(schemaText);
        }
        
        // Otherwise, extract fresh schema
        try {
            logger.info("Extracting fresh schema for project: {}", project.getProjectId());
            SchemaInfo schemaInfo = schemaExtractionService.extractSchema(project);
            
            // Cache it
            String schemaJson = schemaExtractionService.schemaToJson(schemaInfo);
            project.setSchemaText(schemaJson);
            projectRepository.save(project);
            
            return schemaInfo;
        } catch (Exception e) {
            logger.error("Error extracting schema: {}", e.getMessage(), e);
            return new SchemaInfo();
        }
    }
    
    /**
     * Saves a message to chat history
     */
    private void saveMessage(Project project, String sender, String role, String content) {
        try {
            ChatMessage message = new ChatMessage();
            message.setProject(project);
            message.setSender(sender);
            message.setRole(role);
            message.setContent(content);
            message.setTimestamp(LocalDateTime.now());
            
            chatMessageRepository.save(message);
            logger.debug("Message saved: {} - {}", sender, role);
        } catch (Exception e) {
            logger.error("Error saving message: {}", e.getMessage());
        }
    }
    
    /**
     * Tests database connection
     */
    public boolean testConnection(Long projectId) {
        try {
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
            
            return new DatabaseConnectionService().testConnection(project);
        } catch (Exception e) {
            logger.error("Connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets formatted schema as text
     */
    public String getSchemaAsText(Long projectId) throws Exception {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        SchemaInfo schemaInfo = getSchemaInfo(project);
        return schemaExtractionService.formatSchemaForPrompt(schemaInfo);
    }
}
