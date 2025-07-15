package com.querymate.QueryMate.controller;

import com.querymate.QueryMate.dto.ProjectDto;
import com.querymate.QueryMate.payload.ApiResponse;
import com.querymate.QueryMate.service.ProjectService;
import com.querymate.QueryMate.entity.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Project APIs", description = "CRUD operations for project entity")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new project",
            description = "Creates a new project for the authenticated user by providing DB connection details and schema (optional)."
    )
    public ResponseEntity<ProjectDto> createProject(
            @RequestBody ProjectDto projectDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        ProjectDto createdProject = projectService.createProject(projectDto, userId);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    @PutMapping("/{projectId}")
    @Operation(
            summary = "Update project",
            description = "Updates an existing project by project ID. Only editable by its owner."
    )
    public ResponseEntity<ProjectDto> updateProject(
            @PathVariable Long projectId,
            @RequestBody ProjectDto projectDto
    ) {
        ProjectDto updatedProject = projectService.updateProject(projectDto, projectId);
        return ResponseEntity.ok(updatedProject);
    }

    @GetMapping
    @Operation(
            summary = "Get all projects for logged-in user",
            description = "Returns a list of all projects created by the authenticated user."
    )
    public ResponseEntity<List<ProjectDto>> getAllProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        List<ProjectDto> projects = projectService.getAllProjectsByUser(userId);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    @Operation(
            summary = "Get project by ID",
            description = "Retrieves a single project by its ID. Can only be accessed by its owner."
    )
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    @DeleteMapping("/{projectId}")
    @Operation(
            summary = "Delete project",
            description = "Deletes the project with the given ID. Only the owner of the project can delete it."
    )
    public ResponseEntity<ApiResponse> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok(new ApiResponse("Project deleted successfully", true));
    }
}
