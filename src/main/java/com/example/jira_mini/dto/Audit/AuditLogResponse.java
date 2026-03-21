package com.example.jira_mini.dto.Audit;

import com.example.jira_mini.entity.enums.AuditAction;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AuditLogResponse {

  private UUID id;
  private AuditAction action;
  private ActorDto actor;
  private LocalDateTime createdAt;
  private String oldValue;
  private String newValue;

  @Getter
  @Builder
  public static class ActorDto {
    private UUID id;
    private String fullName;
  }
}