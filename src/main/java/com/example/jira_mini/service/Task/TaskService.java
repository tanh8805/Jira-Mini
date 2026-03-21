package com.example.jira_mini.service.Task;

import com.example.jira_mini.dto.Task.CreateTaskRequest;
import com.example.jira_mini.dto.Task.TaskResponse;
import com.example.jira_mini.dto.Task.UpdateTaskRequest;
import com.example.jira_mini.entity.AuditLog;
import com.example.jira_mini.entity.Project;
import com.example.jira_mini.entity.ProjectMember;
import com.example.jira_mini.entity.Task;
import com.example.jira_mini.entity.User;
import com.example.jira_mini.entity.enums.AuditAction;
import com.example.jira_mini.entity.enums.TaskPriority;
import com.example.jira_mini.entity.enums.TaskStatus;
import com.example.jira_mini.exception.*;
import com.example.jira_mini.repository.AuditLogRepository;
import com.example.jira_mini.repository.ProjectMemberRepository;
import com.example.jira_mini.repository.ProjectRepository;
import com.example.jira_mini.repository.TaskRepository;
import com.example.jira_mini.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

  private final TaskRepository taskRepository;
  private final ProjectRepository projectRepository;
  private final ProjectMemberRepository projectMemberRepository;
  private final UserRepository userRepository;
  private final AuditLogRepository auditLogRepository;

  // ── GET /api/projects/{projectId}/tasks ────────────────────────────────────
  @Transactional(readOnly = true)
  public Page<TaskResponse> getTasks(UUID projectId,
                                     TaskStatus status,
                                     TaskPriority priority,
                                     UUID assigneeId,
                                     Pageable pageable) {
    User currentUser = getCurrentUser();
    Project project = getProjectOrThrow(projectId);
    assertMember(project, currentUser);

    return taskRepository
            .findByProjectWithFilters(projectId, status, priority, assigneeId, pageable)
            .map(this::toResponse);
  }

  // ── POST /api/projects/{projectId}/tasks ───────────────────────────────────
  @Transactional
  public TaskResponse createTask(UUID projectId, CreateTaskRequest request) {
    User currentUser = getCurrentUser();
    Project project = getProjectOrThrow(projectId);
    assertMember(project, currentUser);

    User assignee = null;
    if (request.getAssigneeId() != null) {
      assignee = resolveAssignee(request.getAssigneeId(), project);
    }

    Task task = Task.builder()
            .project(project)
            .title(request.getTitle())
            .description(request.getDescription())
            .status(TaskStatus.TODO)
            .priority(request.getPriority())
            .assignee(assignee)
            .build();

    Task savedTask = taskRepository.save(task);

    saveAuditLog(AuditAction.CREATED, savedTask.getId(), null, toSnapshot(savedTask), currentUser);

    return toResponse(savedTask);
  }

  // ── PUT /api/projects/{projectId}/tasks/{taskId} ───────────────────────────
  @Transactional
  public TaskResponse updateTask(UUID projectId, UUID taskId, UpdateTaskRequest request) {
    User currentUser = getCurrentUser();
    Project project = getProjectOrThrow(projectId);
    assertMember(project, currentUser);

    Task task = getTaskOrThrow(taskId);
    assertTaskBelongsToProject(task, projectId);

    String oldSnapshot = toSnapshot(task);

    if (request.getTitle() != null) {
      task.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      task.setDescription(request.getDescription());
    }
    if (request.getStatus() != null) {
      task.setStatus(request.getStatus());
    }
    if (request.getPriority() != null) {
      task.setPriority(request.getPriority());
    }
    if (request.getAssigneeId() != null) {
      User assignee = resolveAssignee(request.getAssigneeId(), project);
      task.setAssignee(assignee);
    } else if (isExplicitlyNulled(request)) {
      task.setAssignee(null);
    }

    Task savedTask = taskRepository.save(task);

    saveAuditLog(AuditAction.UPDATED, taskId, oldSnapshot, toSnapshot(savedTask), currentUser);

    return toResponse(savedTask);
  }

  // ── DELETE /api/projects/{projectId}/tasks/{taskId} ────────────────────────
  @Transactional
  public void deleteTask(UUID projectId, UUID taskId) {
    User currentUser = getCurrentUser();
    Project project = getProjectOrThrow(projectId);
    assertMember(project, currentUser);

    Task task = getTaskOrThrow(taskId);
    assertTaskBelongsToProject(task, projectId);

    String oldSnapshot = toSnapshot(task);

    taskRepository.delete(task);

    saveAuditLog(AuditAction.DELETED, taskId, oldSnapshot, null, currentUser);
  }

  // ── Audit helpers ──────────────────────────────────────────────────────────

  private String toSnapshot(Task task) {
    String assigneeId = task.getAssignee() != null
            ? "\"" + task.getAssignee().getId() + "\""
            : "null";

    return String.format(
            "{\"title\":\"%s\",\"status\":\"%s\",\"priority\":\"%s\",\"assigneeId\":%s}",
            task.getTitle(),
            task.getStatus(),
            task.getPriority(),
            assigneeId
    );
  }

  private void saveAuditLog(AuditAction action, UUID entityId,
                            String oldValue, String newValue, User actor) {
    AuditLog log = AuditLog.builder()
            .entityType("TASK")
            .entityId(entityId)
            .action(action)
            .oldValue(oldValue)
            .newValue(newValue)
            .actor(actor)
            .build();
    auditLogRepository.save(log);
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not authenticated"));
  }

  private Project getProjectOrThrow(UUID projectId) {
    return projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
  }

  private Task getTaskOrThrow(UUID taskId) {
    return taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found"));
  }

  private void assertMember(Project project, User user) {
    boolean isMember = projectMemberRepository
            .existsByProjectAndUser(project, user);
    if (!isMember) {
      throw new UnauthorizedProjectAccessException("You are not a member of this project");
    }
  }

  private void assertTaskBelongsToProject(Task task, UUID projectId) {
    if (!task.getProject().getId().equals(projectId)) {
      throw new TaskNotInProjectException("Task does not belong to the specified project");
    }
  }

  private User resolveAssignee(UUID assigneeId, Project project) {
    User assignee = userRepository.findById(assigneeId)
            .orElseThrow(() -> new TaskNotFoundException("Assignee user not found"));
    boolean assigneeMember = projectMemberRepository
            .existsByProjectAndUser(project, assignee);
    if (!assigneeMember) {
      throw new AssigneeNotMemberException("Assignee is not a member of this project");
    }
    return assignee;
  }

  private boolean isExplicitlyNulled(UpdateTaskRequest request) {
    return false;
  }

  private TaskResponse toResponse(Task task) {
    return TaskResponse.builder()
            .id(task.getId())
            .title(task.getTitle())
            .description(task.getDescription())
            .status(task.getStatus())
            .priority(task.getPriority())
            .projectId(task.getProject().getId())
            .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
            .assigneeName(task.getAssignee() != null ? task.getAssignee().getFullName() : null)
            .createdAt(task.getCreatedAt())
            .build();
  }
}