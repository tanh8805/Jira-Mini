package com.example.jira_mini.dto.Project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;
import java.io.*;

@Getter
@Builder
@AllArgsConstructor
public class ProjectResponse {
  private UUID id;
  private String name;
  private String description;
  private UserResponse owner;
  private LocalDateTime createdAt;
}
