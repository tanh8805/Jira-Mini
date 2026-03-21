package com.example.jira_mini.exception;

public class UnauthorizedProjectAccessException extends RuntimeException {
  public UnauthorizedProjectAccessException(String message) {
    super(message);
  }
}