package com.example.jira_mini.repository;

import com.example.jira_mini.entity.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

  @EntityGraph(attributePaths = {"project"})
  List<Task> findByAssigneeId(UUID assigneeId);
}