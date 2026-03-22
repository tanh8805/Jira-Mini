package com.example.jira_mini.controller.Project;

import com.example.jira_mini.dto.Project.UpdateProjectRequest;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Quản lý project")
public class UpdateProjectController {

    private final ProjectService projectService;

    @PutMapping("/{projectId}")
    @Operation(summary = "Cập nhật project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "403", description = "Không phải OWNER"),
            @ApiResponse(responseCode = "404", description = "Project hoặc User không tồn tại")
    })
    public ResponseEntity<?> updateProject(
            @PathVariable UUID projectId,
            Authentication authentication,
            @Valid @RequestBody UpdateProjectRequest request) {
        String requesterEmail = authentication.getName();
        Project updated = projectService.updateProject(projectId, requesterEmail, request.getName(), request.getDescription());
        return ResponseEntity.ok(updated);
    }
}