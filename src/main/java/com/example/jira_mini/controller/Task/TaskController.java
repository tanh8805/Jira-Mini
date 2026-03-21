package com.example.jira_mini.controller.Task;

import com.example.jira_mini.dto.Task.CreateTaskRequest;
import com.example.jira_mini.dto.Task.TaskResponse;
import com.example.jira_mini.dto.Task.UpdateTaskRequest;
import com.example.jira_mini.entity.enums.TaskPriority;
import com.example.jira_mini.entity.enums.TaskStatus;
import com.example.jira_mini.service.Task.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController {

  private final TaskService taskService;

  @GetMapping("/api/projects/{projectId}/tasks")
  public ResponseEntity<Page<TaskResponse>> getTasks(
          @PathVariable UUID projectId,
          @RequestParam(required = false) TaskStatus status,
          @RequestParam(required = false) TaskPriority priority,
          @RequestParam(required = false) UUID assigneeId,
          @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
  ) {
    return ResponseEntity.ok(
            taskService.getTasks(projectId, status, priority, assigneeId, pageable)
    );
  }

  @PostMapping("/api/projects/{projectId}/tasks")
  public ResponseEntity<TaskResponse> createTask(
          @PathVariable UUID projectId,
          @Valid @RequestBody CreateTaskRequest request
  ) {
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(taskService.createTask(projectId, request));
  }

  @PutMapping("/api/projects/{projectId}/tasks/{taskId}")
  public ResponseEntity<TaskResponse> updateTask(
          @PathVariable UUID projectId,
          @PathVariable UUID taskId,
          @Valid @RequestBody UpdateTaskRequest request
  ) {
    return ResponseEntity.ok(
            taskService.updateTask(projectId, taskId, request)
    );
  }

  @DeleteMapping("/api/projects/{projectId}/tasks/{taskId}")
  public ResponseEntity<Void> deleteTask(
          @PathVariable UUID projectId,
          @PathVariable UUID taskId
  ) {
    taskService.deleteTask(projectId, taskId);
    return ResponseEntity.noContent().build();
  }
}