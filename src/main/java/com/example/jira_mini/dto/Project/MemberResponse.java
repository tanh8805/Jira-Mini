package com.example.jira_mini.dto.Project;

import com.example.jira_mini.entity.enums.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
  private UUID userId;
  private String email;
  private String fullName;
  private ProjectRole role;
  private LocalDateTime joinedAt;
}