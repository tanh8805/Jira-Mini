// ─── CreateTaskRequest.java ───────────────────────────────────────────────────
package com.example.jira_mini.dto.Task;

import com.example.jira_mini.entity.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateTaskRequest {

  @NotBlank(message = "Title must not be blank")
  @Size(max = 100, message = "Title must not exceed 100 characters")
  private String title;

  @Size(max = 2000, message = "Description must not exceed 2000 characters")
  private String description;

  @NotNull(message = "Priority is required")
  private TaskPriority priority;

  private UUID assigneeId; // optional at creation
}