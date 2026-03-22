package com.example.jira_mini.controller.Project;

import com.example.jira_mini.dto.Project.MemberResponse;
import com.example.jira_mini.service.Project.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Quản lý project")
public class GetMembersController {

  private final ProjectService projectService;

  @GetMapping("/{projectId}/members")
  @Operation(summary = "Lấy danh sách thành viên của project")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Thành công"),
          @ApiResponse(responseCode = "403", description = "Không phải member của project"),
          @ApiResponse(responseCode = "404", description = "Project không tồn tại")
  })
  public ResponseEntity<List<MemberResponse>> getMembers(
          @PathVariable UUID projectId,
          Authentication authentication) {
    String email = authentication.getName();
    List<MemberResponse> members = projectService.getProjectMembers(projectId, email);
    return ResponseEntity.ok(members);
  }
}