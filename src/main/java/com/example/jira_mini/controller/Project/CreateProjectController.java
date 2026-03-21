package com.example.jira_mini.controller.Project;

import com.example.jira_mini.dto.Project.CreateProjectRequest;
import com.example.jira_mini.entity.Project;
import com.example.jira_mini.service.Project.ProjectService;
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
public class CreateProjectController {

  private final ProjectService projectService;

  @PostMapping
  public ResponseEntity<?> createProject(
          Authentication authentication,
          @Valid @RequestBody CreateProjectRequest request) {
    String email = authentication.getName();
    Project created = projectService.createProject(email, request.getName(), request.getDescription());
    return ResponseEntity.status(201).body(created);
  }
}