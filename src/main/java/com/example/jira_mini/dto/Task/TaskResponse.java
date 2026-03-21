package com.example.jira_mini.dto.Task;

import com.example.jira_mini.entity.enums.TaskPriority;
import com.example.jira_mini.entity.enums.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TaskResponse {
    private UUID id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private UUID projectId;
    private UUID assigneeId;
    private String assigneeName;
    private LocalDateTime createdAt;
}