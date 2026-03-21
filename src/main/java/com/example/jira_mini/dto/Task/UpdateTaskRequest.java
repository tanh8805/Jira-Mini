package com.example.jira_mini.dto.Task;

import com.example.jira_mini.entity.enums.TaskPriority;
import com.example.jira_mini.entity.enums.TaskStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateTaskRequest {

  @Size(max = 100, message = "Title must not exceed 100 characters")
  private String title;

  @Size(max = 2000, message = "Description must not exceed 2000 characters")
  private String description;

  private TaskStatus status;

  private TaskPriority priority;

  private UUID assigneeId; // null = unassign
}