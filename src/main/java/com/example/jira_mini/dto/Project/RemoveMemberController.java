package com.example.jira_mini.controller.Project;

import com.example.jira_mini.service.Project.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Quản lý project")
public class RemoveMemberController {

  private final ProjectService projectService;

  @DeleteMapping("/{projectId}/members/{userId}")
  @Operation(summary = "Xoá thành viên khỏi project (hoặc tự rời)")
  @ApiResponses({
          @ApiResponse(responseCode = "204", description = "Xoá thành công"),
          @ApiResponse(responseCode = "400", description = "OWNER không thể tự rời project"),
          @ApiResponse(responseCode = "403", description = "Không đủ quyền"),
          @ApiResponse(responseCode = "404", description = "Project hoặc Member không tồn tại")
  })
  public ResponseEntity<Void> removeMember(
          @PathVariable UUID projectId,
          @PathVariable UUID userId,
          Authentication authentication) {
    String requesterEmail = authentication.getName();
    projectService.removeMember(projectId, userId, requesterEmail);
    return ResponseEntity.noContent().build();
  }
}