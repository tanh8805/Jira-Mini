package com.example.jira_mini.exception;

public class MemberAlreadyExistsException extends RuntimeException {
  public MemberAlreadyExistsException(String email) {
    super("User with email '" + email + "' is already a member of this project");
  }
}