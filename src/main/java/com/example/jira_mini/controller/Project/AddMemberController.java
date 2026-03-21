package com.example.jira_mini.controller.Project;

import com.example.jira_mini.dto.Project.AddMemberRequest;
import com.example.jira_mini.entity.ProjectMember;
import com.example.jira_mini.service.Project.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class AddMemberController {

  private final ProjectService projectService;

  @PostMapping("/{projectId}/members")
  public ResponseEntity<?> addMember(
          @PathVariable UUID projectId,
          Authentication authentication,
          @Valid @RequestBody AddMemberRequest request) {
    String requesterEmail = authentication.getName();
    ProjectMember member = projectService.addMember(projectId, requesterEmail, request.getEmail(), request.getRole());
    return ResponseEntity.status(201).body(member);
  }
}