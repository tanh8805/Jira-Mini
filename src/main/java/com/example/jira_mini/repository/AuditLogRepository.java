package com.example.jira_mini.repository;

import com.example.jira_mini.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
  List<AuditLog> findByEntityIdOrderByCreatedAtDesc(UUID entityId);
}