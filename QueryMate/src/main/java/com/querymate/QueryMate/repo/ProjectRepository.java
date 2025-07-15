package com.querymate.QueryMate.repo;

import com.querymate.QueryMate.entity.Project;
import com.querymate.QueryMate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Get all projects by user
    List<Project> findByUser(User user);
}
