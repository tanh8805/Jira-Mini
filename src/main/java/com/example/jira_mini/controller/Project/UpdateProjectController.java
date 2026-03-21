package com.example.jira_mini.controller.Project;

import com.example.jira_mini.dto.Project.UpdateProjectRequest;
import com.example.jira_mini.entity.Project;
import com.example.jira_mini.service.Project.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class UpdateProjectController {

    private final ProjectService projectService;

    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(
            @PathVariable UUID projectId,
            Authentication authentication,
            @Valid @RequestBody UpdateProjectRequest request) {
        String requesterEmail = authentication.getName();
        Project updated = projectService.updateProject(projectId, requesterEmail, request.getName(), request.getDescription());
        return ResponseEntity.ok(updated);
    }
}