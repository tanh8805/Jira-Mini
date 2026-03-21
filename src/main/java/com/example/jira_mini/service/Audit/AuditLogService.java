package com.example.jira_mini.service.Audit;

import com.example.jira_mini.dto.Audit.AuditLogResponse;
import com.example.jira_mini.entity.AuditLog;
import com.example.jira_mini.entity.Project;
import com.example.jira_mini.entity.Task;
import com.example.jira_mini.entity.User;
import com.example.jira_mini.exception.ProjectNotFoundException;
import com.example.jira_mini.exception.TaskNotFoundException;
import com.example.jira_mini.exception.UnauthorizedProjectAccessException;
import com.example.jira_mini.exception.UserNotFoundException;
import com.example.jira_mini.repository.AuditLogRepository;
import com.example.jira_mini.repository.ProjectMemberRepository;
import com.example.jira_mini.repository.TaskRepository;
import com.example.jira_mini.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;
  private final TaskRepository taskRepository;
  private final ProjectMemberRepository projectMemberRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public Page<AuditLogResponse> getAuditLogs(String entityType, UUID entityId, Pageable pageable) {
    if (!"TASK".equalsIgnoreCase(entityType)) {
      throw new IllegalArgumentException("entityType must be TASK");
    }

    User currentUser = getCurrentUser();

    Task task = taskRepository.findById(entityId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found"));

    Project project = task.getProject();

    boolean isMember = projectMemberRepository.existsByProjectAndUser(project, currentUser);
    if (!isMember) {
      throw new UnauthorizedProjectAccessException("You are not a member of this project");
    }

    return auditLogRepository
            .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("TASK", entityId, pageable)
            .map(this::toResponse);
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not authenticated"));
  }

  private AuditLogResponse toResponse(AuditLog log) {
    AuditLogResponse.ActorDto actorDto = null;
    if (log.getActor() != null) {
      actorDto = AuditLogResponse.ActorDto.builder()
              .id(log.getActor().getId())
              .fullName(log.getActor().getFullName())
              .build();
    }

    return AuditLogResponse.builder()
            .id(log.getId())
            .action(log.getAction())
            .actor(actorDto)
            .createdAt(log.getCreatedAt())
            .oldValue(log.getOldValue())
            .newValue(log.getNewValue())
            .build();
  }
}