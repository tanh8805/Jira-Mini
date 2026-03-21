package com.example.jira_mini.exception;

import java.util.UUID;

public class ProjectNotFoundException extends RuntimeException {
  public ProjectNotFoundException(UUID id) {
    super("Project not found with id: " + id);
  }
}