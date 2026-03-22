package com.example.jira_mini.controller.Audit;

import com.example.jira_mini.dto.Audit.AuditLogResponse;
import com.example.jira_mini.service.Audit.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Log", description = "Lịch sử thay đổi task")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Xem lịch sử thay đổi của task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "400", description = "entityType không phải TASK"),
            @ApiResponse(responseCode = "403", description = "Không phải member của project chứa task"),
            @ApiResponse(responseCode = "404", description = "Task không tồn tại")
    })
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestParam String entityType,
            @RequestParam UUID entityId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(auditLogService.getAuditLogs(entityType, entityId, pageable));
    }
}