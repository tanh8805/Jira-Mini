package com.example.jira_mini.controller.Project;

import com.example.jira_mini.dto.Project.ProjectResponse;
import com.example.jira_mini.entity.Project;
import com.example.jira_mini.service.Project.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Quản lý project")
public class GetProjectController {

  private final ProjectService projectService;

  @GetMapping
  @Operation(summary = "Lấy danh sách project của user")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Thành công"),
          @ApiResponse(responseCode = "404", description = "User không tồn tại")
  })
  public ResponseEntity<?> getMyProjects(Authentication authentication) {
    String email = authentication.getName();
    List<ProjectResponse> projects = projectService.getProjectsByUserEmail(email);
    return ResponseEntity.ok(projects);
  }
}