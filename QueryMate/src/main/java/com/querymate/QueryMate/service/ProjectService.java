package com.querymate.QueryMate.service;

import com.querymate.QueryMate.dto.ProjectDto;

import java.util.List;

public interface ProjectService {
    ProjectDto createProject(ProjectDto projectDto, Long userId);

    ProjectDto updateProject(ProjectDto projectDto, Long projectId);

    List<ProjectDto> getAllProjectsByUser(Long userId);

    ProjectDto getProjectById(Long projectId);

    void deleteProject(Long projectId);
}
