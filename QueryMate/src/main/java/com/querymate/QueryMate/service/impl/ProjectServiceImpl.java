package com.querymate.QueryMate.service.impl;

import com.querymate.QueryMate.dto.ProjectDto;
import com.querymate.QueryMate.entity.Project;
import com.querymate.QueryMate.entity.User;
import com.querymate.QueryMate.exception.ResourceNotFoundException;
import com.querymate.QueryMate.repo.ProjectRepository;
import com.querymate.QueryMate.repo.UserRepository;
import com.querymate.QueryMate.service.ProjectService;
import com.querymate.QueryMate.service.SchemaService;
import com.querymate.QueryMate.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CryptoUtils cryptoUtils;
    private final SchemaService schemaService;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository, CryptoUtils cryptoUtils, SchemaService schemaService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.cryptoUtils = cryptoUtils;
        this.schemaService = schemaService;
    }

    @Override
    public ProjectDto createProject(ProjectDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Project project = mapToEntity(dto);
        project.setUser(user);
        project.setCreatedAt(LocalDateTime.now());

        // 🔐 Decrypt credentials to connect to DB
        String username = cryptoUtils.decrypt(project.getDbUsername());
        String password = cryptoUtils.decrypt(project.getDbPassword());

        // 🌐 Attempt to extract schema from the actual database
        String extractedSchema = schemaService.getSchemaForProject(project.getProjectId());

        // Always set schemaText to whatever message was returned
        project.setSchemaText(extractedSchema != null ? extractedSchema.trim() : "⚠️ Failed to retrieve schema.");

        Project saved = projectRepository.save(project);
        return mapToDto(saved);
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
        project.setDbHost(projectDto.getDbHost() != null ? projectDto.getDbHost() : project.getDbHost());
        project.setDbPort(projectDto.getDbPort() != null ? projectDto.getDbPort() : project.getDbPort());
        project.setDbUsername(projectDto.getDbUsername() != null ? projectDto.getDbUsername() : project.getDbUsername());
        project.setDbPassword(projectDto.getDbPassword() != null ? projectDto.getDbPassword() : project.getDbPassword());
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
        dto.setDbHost(project.getDbHost());
        dto.setDbPort(project.getDbPort());

        // ✅ Decrypt sensitive fields
        dto.setDbUsername(cryptoUtils.decrypt(project.getDbUsername()));
        dto.setDbPassword(cryptoUtils.decrypt(project.getDbPassword()));

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
        project.setDbHost(dto.getDbHost());
        project.setDbPort(dto.getDbPort());

        // ✅ Encrypt sensitive fields
        project.setDbUsername(cryptoUtils.encrypt(dto.getDbUsername()));
        project.setDbPassword(cryptoUtils.encrypt(dto.getDbPassword()));

        project.setDbName(dto.getDbName());
        return project;
    }
}
