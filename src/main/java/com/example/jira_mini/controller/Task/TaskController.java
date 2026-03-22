package com.example.jira_mini.controller.Task;

import com.example.jira_mini.dto.Task.CreateTaskRequest;
import com.example.jira_mini.dto.Task.TaskResponse;
import com.example.jira_mini.dto.Task.UpdateTaskRequest;
import com.example.jira_mini.entity.enums.TaskPriority;
import com.example.jira_mini.entity.enums.TaskStatus;
import com.example.jira_mini.service.Task.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Task", description = "Quản lý task")
public class TaskController {

  private final TaskService taskService;

  @GetMapping("/api/projects/{projectId}/tasks")
  @Operation(summary = "Lấy danh sách task (có filter + pagination)")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Thành công"),
          @ApiResponse(responseCode = "403", description = "Không phải member của project"),
          @ApiResponse(responseCode = "404", description = "Project không tồn tại")
  })
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
  @Operation(summary = "Tạo task mới")
  @ApiResponses({
          @ApiResponse(responseCode = "201", description = "Tạo thành công"),
          @ApiResponse(responseCode = "400", description = "Validation lỗi hoặc assignee không phải member"),
          @ApiResponse(responseCode = "403", description = "Không phải member của project"),
          @ApiResponse(responseCode = "404", description = "Project không tồn tại")
  })
  public ResponseEntity<TaskResponse> createTask(
          @PathVariable UUID projectId,
          @Valid @RequestBody CreateTaskRequest request
  ) {
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(taskService.createTask(projectId, request));
  }

  @PutMapping("/api/projects/{projectId}/tasks/{taskId}")
  @Operation(summary = "Cập nhật task")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
          @ApiResponse(responseCode = "400", description = "Assignee không phải member"),
          @ApiResponse(responseCode = "403", description = "Không phải member của project"),
          @ApiResponse(responseCode = "404", description = "Project hoặc Task không tồn tại")
  })
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
  @Operation(summary = "Xoá task")
  @ApiResponses({
          @ApiResponse(responseCode = "204", description = "Xoá thành công"),
          @ApiResponse(responseCode = "403", description = "Không phải member của project"),
          @ApiResponse(responseCode = "404", description = "Project hoặc Task không tồn tại")
  })
  public ResponseEntity<Void> deleteTask(
          @PathVariable UUID projectId,
          @PathVariable UUID taskId
  ) {
    taskService.deleteTask(projectId, taskId);
    return ResponseEntity.noContent().build();
  }
}