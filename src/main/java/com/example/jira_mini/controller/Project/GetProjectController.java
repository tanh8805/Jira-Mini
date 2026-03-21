package com.example.jira_mini.controller.Project;

import com.example.jira_mini.entity.Project;
import com.example.jira_mini.service.Project.ProjectService;
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
public class GetProjectController {

  private final ProjectService projectService;

  @GetMapping
  public ResponseEntity<?> getMyProjects(Authentication authentication) {
    String email = authentication.getName();
    List<Project> projects = projectService.getProjectsByUserEmail(email);
    return ResponseEntity.ok(projects);
  }
}