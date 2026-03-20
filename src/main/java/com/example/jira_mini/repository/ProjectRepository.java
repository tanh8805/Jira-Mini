package com.example.jira_mini.repository;

import com.example.jira_mini.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

  @EntityGraph(attributePaths = {"owner"})
  Optional<Project> findById(UUID id);
}