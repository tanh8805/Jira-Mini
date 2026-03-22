package com.example.jira_mini.controller.Project;

import com.example.jira_mini.dto.Project.CreateProjectRequest;
import com.example.jira_mini.entity.Project;
import com.example.jira_mini.service.Project.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Quản lý project")
public class CreateProjectController {

  private final ProjectService projectService;

  @PostMapping
  @Operation(summary = "Tạo project mới")
  @ApiResponses({
          @ApiResponse(responseCode = "201", description = "Tạo thành công"),
          @ApiResponse(responseCode = "404", description = "User không tồn tại")
  })
  public ResponseEntity<?> createProject(
          Authentication authentication,
          @Valid @RequestBody CreateProjectRequest request) {
    String email = authentication.getName();
    Project created = projectService.createProject(email, request.getName(), request.getDescription());
    return ResponseEntity.status(201).body(created);
  }
}