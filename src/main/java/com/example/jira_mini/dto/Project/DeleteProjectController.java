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
public class DeleteProjectController {

  private final ProjectService projectService;

  @DeleteMapping("/{projectId}")
  @Operation(summary = "Xoá project (chỉ OWNER)")
  @ApiResponses({
          @ApiResponse(responseCode = "204", description = "Xoá thành công"),
          @ApiResponse(responseCode = "403", description = "Không phải OWNER"),
          @ApiResponse(responseCode = "404", description = "Project không tồn tại")
  })
  public ResponseEntity<Void> deleteProject(
          @PathVariable UUID projectId,
          Authentication authentication) {
    String requesterEmail = authentication.getName();
    projectService.deleteProject(projectId, requesterEmail);
    return ResponseEntity.noContent().build();
  }
}