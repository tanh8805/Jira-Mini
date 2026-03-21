package com.example.jira_mini.repository;

import com.example.jira_mini.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
  Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
          String entityType, UUID entityId, Pageable pageable);
}