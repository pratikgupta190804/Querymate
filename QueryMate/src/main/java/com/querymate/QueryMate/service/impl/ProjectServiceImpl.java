package com.querymate.QueryMate.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querymate.QueryMate.dto.ProjectDto;
import com.querymate.QueryMate.entity.Project;
import com.querymate.QueryMate.entity.User;
import com.querymate.QueryMate.exception.ResourceNotFoundException;
import com.querymate.QueryMate.repo.ProjectRepository;
import com.querymate.QueryMate.repo.UserRepository;
import com.querymate.QueryMate.service.ProjectService;
import com.querymate.QueryMate.service.SchemaExtractionService;
import com.querymate.QueryMate.utils.CryptoUtils;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CryptoUtils cryptoUtils;
    private final SchemaExtractionService schemaExtractionService;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository, CryptoUtils cryptoUtils, SchemaExtractionService schemaExtractionService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.cryptoUtils = cryptoUtils;
        this.schemaExtractionService = schemaExtractionService;
    }

    @Override
    public ProjectDto createProject(ProjectDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Project project = mapToEntity(dto);
        project.setUser(user);
        project.setCreatedAt(LocalDateTime.now());

        // Save project first to generate the ID
        Project saved = projectRepository.save(project);

        // 🌐 Extract schema from the actual database
        try {
            var schemaInfo = schemaExtractionService.extractSchema(saved);
            String extractedSchema = schemaExtractionService.schemaToJson(schemaInfo);
            saved.setSchemaText(extractedSchema != null ? extractedSchema.trim() : "{}");
        } catch (Exception e) {
            saved.setSchemaText("{}");
        }

        // Save again with schema
        Project updated = projectRepository.save(saved);
        return mapToDto(updated);
    }

    @Override
    public ProjectDto updateProject(ProjectDto projectDto, Long projectId) {
        // Step 1: Fetch the project from the repository
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // Step 2: Update fields if new values are provided
        project.setProjectName(projectDto.getProjectName() != null ? projectDto.getProjectName() : project.getProjectName());
        project.setDescription(projectDto.getDescription() != null ? projectDto.getDescription() : project.getDescription());
        project.setDbType(projectDto.getDbType() != null ? projectDto.getDbType() : project.getDbType());
        project.setConnectionType(projectDto.getConnectionType() != null ? projectDto.getConnectionType() : project.getConnectionType());
        project.setCloudProvider(projectDto.getCloudProvider() != null ? projectDto.getCloudProvider() : project.getCloudProvider());
        
        if (projectDto.getCloudConnectionString() != null && !projectDto.getCloudConnectionString().isEmpty()) {
            project.setCloudConnectionString(cryptoUtils.encrypt(projectDto.getCloudConnectionString()));
        }
        
        project.setDbHost(projectDto.getDbHost() != null ? projectDto.getDbHost() : project.getDbHost());
        project.setDbPort(projectDto.getDbPort() != null ? projectDto.getDbPort() : project.getDbPort());
        
        // Encrypt credentials if they are being updated
        if (projectDto.getDbUsername() != null) {
            project.setDbUsername(cryptoUtils.encrypt(projectDto.getDbUsername()));
        }
        if (projectDto.getDbPassword() != null) {
            project.setDbPassword(cryptoUtils.encrypt(projectDto.getDbPassword()));
        }
        
        project.setDbName(projectDto.getDbName() != null ? projectDto.getDbName() : project.getDbName());

        // Step 3: Save the updated project
        Project updatedProject = projectRepository.save(project);

        // Step 4: Return the updated project as a DTO
        return this.mapToDto(updatedProject); // Replace with your actual mapper method
    }


    @Override
    public List<ProjectDto> getAllProjectsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Project> projects = projectRepository.findByUser(user);
        return projects.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public ProjectDto getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        return mapToDto(project);
    }

    @Override
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        projectRepository.delete(project);
    }

    // 🔁 Mapping methods

    private ProjectDto mapToDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setProjectId(project.getProjectId());
        dto.setProjectName(project.getProjectName());
        dto.setDescription(project.getDescription());
        dto.setDbType(project.getDbType());
        dto.setConnectionType(project.getConnectionType());
        dto.setCloudProvider(project.getCloudProvider());
        dto.setDbHost(project.getDbHost());
        dto.setDbPort(project.getDbPort());

        // ✅ Decrypt sensitive fields (with error handling for legacy unencrypted data)
        try {
            dto.setDbUsername(cryptoUtils.decrypt(project.getDbUsername()));
            dto.setDbPassword(cryptoUtils.decrypt(project.getDbPassword()));
            if (project.getCloudConnectionString() != null && !project.getCloudConnectionString().isEmpty()) {
                dto.setCloudConnectionString(cryptoUtils.decrypt(project.getCloudConnectionString()));
            }
        } catch (Exception e) {
            // If decryption fails, the data might not be encrypted - use as is
            dto.setDbUsername(project.getDbUsername());
            dto.setDbPassword(project.getDbPassword());
            dto.setCloudConnectionString(project.getCloudConnectionString());
        }

        dto.setDbName(project.getDbName());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUserId(project.getUser().getUserId());
        return dto;
    }

    private Project mapToEntity(ProjectDto dto) {
        Project project = new Project();
        project.setProjectName(dto.getProjectName());
        project.setDescription(dto.getDescription());
        project.setDbType(dto.getDbType());
        project.setConnectionType(dto.getConnectionType());
        project.setCloudProvider(dto.getCloudProvider());
        project.setDbHost(dto.getDbHost());
        project.setDbPort(dto.getDbPort());

        // ✅ Encrypt sensitive fields
        project.setDbUsername(cryptoUtils.encrypt(dto.getDbUsername()));
        project.setDbPassword(cryptoUtils.encrypt(dto.getDbPassword()));
        
        // Encrypt cloud connection string if provided
        if (dto.getCloudConnectionString() != null && !dto.getCloudConnectionString().isEmpty()) {
            project.setCloudConnectionString(cryptoUtils.encrypt(dto.getCloudConnectionString()));
        }

        project.setDbName(dto.getDbName());
        return project;
    }
}
