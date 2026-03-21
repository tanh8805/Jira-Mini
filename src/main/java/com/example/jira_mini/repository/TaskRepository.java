package com.example.jira_mini.repository;

import com.example.jira_mini.entity.Task;
import com.example.jira_mini.entity.enums.TaskPriority;
import com.example.jira_mini.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

  @Query("""
            SELECT t FROM Task t
            WHERE t.project.id = :projectId
              AND (:status IS NULL OR t.status = :status)
              AND (:priority IS NULL OR t.priority = :priority)
              AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
            """)
  Page<Task> findByProjectWithFilters(
          @Param("projectId") UUID projectId,
          @Param("status") TaskStatus status,
          @Param("priority") TaskPriority priority,
          @Param("assigneeId") UUID assigneeId,
          Pageable pageable
  );
}