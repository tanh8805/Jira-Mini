package com.example.jira_mini.repository;

import com.example.jira_mini.dto.Project.MemberResponse;
import com.example.jira_mini.dto.Project.ProjectResponse;
import com.example.jira_mini.entity.Project;
import com.example.jira_mini.entity.ProjectMember;
import com.example.jira_mini.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

  @EntityGraph(attributePaths = {"user"})
  List<ProjectMember> findByProjectId(UUID projectId);

  Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);


  @Query("""
        SELECT new com.example.jira_mini.dto.Project.ProjectResponse(
            p.id,
            p.name,
            p.description,
            new com.example.jira_mini.dto.Project.UserResponse(
                u.id, u.email, u.fullName, u.role
            ),
            p.createdAt
        )
        FROM ProjectMember pm
        JOIN pm.project p
        JOIN p.owner u
        WHERE pm.user.id = :userId
    """)
  List<ProjectResponse> findProjectsByUserId(@Param("userId") UUID userId);

  @EntityGraph(attributePaths = {"user"})
  List<ProjectMember> findByProjectId(UUID projectId);

  boolean existsByProjectAndUser(Project project, User user);
}