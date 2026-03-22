package com.example.jira_mini.service.Project;

import com.example.jira_mini.dto.Project.MemberResponse;
import com.example.jira_mini.dto.Project.ProjectResponse;
import com.example.jira_mini.entity.Project;
import com.example.jira_mini.entity.ProjectMember;
import com.example.jira_mini.entity.User;
import com.example.jira_mini.entity.enums.ProjectRole;
import com.example.jira_mini.exception.MemberAlreadyExistsException;
import com.example.jira_mini.exception.MemberNotFoundException;
import com.example.jira_mini.exception.ProjectNotFoundException;
import com.example.jira_mini.exception.UnauthorizedProjectAccessException;
import com.example.jira_mini.exception.UserNotFoundException;
import com.example.jira_mini.repository.ProjectMemberRepository;
import com.example.jira_mini.repository.ProjectRepository;
import com.example.jira_mini.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final ProjectMemberRepository projectMemberRepository;
  private final UserRepository userRepository;

  // =========================================================
  // 1. Lấy danh sách project mà user (theo email) là thành viên
  // =========================================================
  @Transactional(readOnly = true)
  public List<ProjectResponse> getProjectsByUserEmail(String email) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    return projectMemberRepository.findProjectsByUserId(user.getId());
  }

  // =========================================================
  // 2. Tạo mới project — người tạo tự động là OWNER
  // =========================================================
  @Transactional
  public Project createProject(String ownerEmail, String name, String description) {
    User owner = userRepository.findByEmail(ownerEmail)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + ownerEmail));

    Project project = Project.builder()
            .name(name)
            .description(description)
            .owner(owner)
            .build();

    Project saved = projectRepository.save(project);

    ProjectMember ownerMember = ProjectMember.builder()
            .project(saved)
            .user(owner)
            .role(ProjectRole.OWNER)
            .build();

    projectMemberRepository.save(ownerMember);
    return saved;
  }

  // =========================================================
  // 3. Cập nhật project — chỉ OWNER mới được phép
  // =========================================================
  @Transactional
  public Project updateProject(UUID projectId, String requesterEmail, String name, String description) {
    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));

    ProjectMember requester = getMemberOrThrow(projectId, requesterEmail);

    if (requester.getRole() != ProjectRole.OWNER) {
      throw new UnauthorizedProjectAccessException("Only OWNER can update the project");
    }

    if (name != null && !name.isBlank()) project.setName(name);
    if (description != null) project.setDescription(description);

    return projectRepository.save(project);
  }

  // =========================================================
  // 4. Thêm thành viên — OWNER và MANAGER đều được phép
  // =========================================================
  @Transactional
  public ProjectMember addMember(UUID projectId, String requesterEmail, String memberEmail, ProjectRole role) {
    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));

    ProjectMember requester = getMemberOrThrow(projectId, requesterEmail);

    if (requester.getRole() != ProjectRole.OWNER && requester.getRole() != ProjectRole.MANAGER) {
      throw new UnauthorizedProjectAccessException("Only OWNER or MANAGER can add members");
    }

    User newUser = userRepository.findByEmail(memberEmail)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + memberEmail));

    projectMemberRepository.findByProjectIdAndUserId(projectId, newUser.getId())
            .ifPresent(m -> { throw new MemberAlreadyExistsException(memberEmail); });

    ProjectMember member = ProjectMember.builder()
            .project(project)
            .user(newUser)
            .role(role)
            .build();

    return projectMemberRepository.save(member);
  }

  // =========================================================
  // 5. Lấy danh sách thành viên của project
  // =========================================================
  @Transactional(readOnly = true)
  public List<MemberResponse> getProjectMembers(UUID projectId, String requesterEmail) {
    if (!projectRepository.existsById(projectId)) {
      throw new ProjectNotFoundException(projectId);
    }
    getMemberOrThrow(projectId, requesterEmail);

    return projectMemberRepository.findByProjectId(projectId)
            .stream()
            .map(pm -> new MemberResponse(
                    pm.getUser().getId(),
                    pm.getUser().getEmail(),
                    pm.getUser().getFullName(),
                    pm.getRole(),
                    pm.getJoinedAt()
            ))
            .toList();
  }

  // =========================================================
  // 6. Xoá thành viên khỏi project
  //
  //    Rules:
  //    - Self-leave: MANAGER/MEMBER được, OWNER KHÔNG được
  //    - OWNER: xoá được MANAGER và MEMBER
  //    - MANAGER: chỉ xoá được MEMBER
  //    - MEMBER: không xoá được người khác
  // =========================================================
  @Transactional
  public void removeMember(UUID projectId, UUID targetUserId, String requesterEmail) {
    if (!projectRepository.existsById(projectId)) {
      throw new ProjectNotFoundException(projectId);
    }

    ProjectMember requester = getMemberOrThrow(projectId, requesterEmail);

    ProjectMember target = projectMemberRepository.findByProjectIdAndUserId(projectId, targetUserId)
            .orElseThrow(() -> new MemberNotFoundException("Member not found in this project"));

    boolean isSelf = requester.getId().equals(target.getId());

    if (isSelf) {
      // OWNER không được tự rời — phải transfer ownership trước
      if (requester.getRole() == ProjectRole.OWNER) {
        throw new UnauthorizedProjectAccessException(
                "OWNER cannot leave the project. Transfer ownership first.");
      }
      // MANAGER / MEMBER tự rời: cho phép
    } else {
      // Xoá người khác
      if (requester.getRole() == ProjectRole.MEMBER) {
        throw new UnauthorizedProjectAccessException("MEMBER cannot remove other members");
      }
      if (requester.getRole() == ProjectRole.MANAGER && target.getRole() != ProjectRole.MEMBER) {
        throw new UnauthorizedProjectAccessException("MANAGER can only remove MEMBER");
      }
      // OWNER xoá bất kỳ: cho phép
    }

    projectMemberRepository.delete(target);
  }

  // =========================================================
  // 7. Xoá project — chỉ OWNER
  //    Tasks & members bị cascade delete bởi DB (ON DELETE CASCADE)
  // =========================================================
  @Transactional
  public void deleteProject(UUID projectId, String requesterEmail) {
    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));

    ProjectMember requester = getMemberOrThrow(projectId, requesterEmail);

    if (requester.getRole() != ProjectRole.OWNER) {
      throw new UnauthorizedProjectAccessException("Only OWNER can delete the project");
    }

    projectMemberRepository.deleteAllByProjectId(projectId);
    projectRepository.delete(project);
  }

  // =========================================================
  // Helper: lấy ProjectMember hoặc throw nếu không phải member
  // =========================================================
  private ProjectMember getMemberOrThrow(UUID projectId, String email) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

    return projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId())
            .orElseThrow(() -> new UnauthorizedProjectAccessException("You are not a member of this project"));
  }
}