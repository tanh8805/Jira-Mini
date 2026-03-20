package com.example.jira_mini.repository;

import com.example.jira_mini.entity.ProjectMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

  @EntityGraph(attributePaths = {"user"})
  List<ProjectMember> findByProjectId(UUID projectId);

  Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);
}