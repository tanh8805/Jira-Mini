package com.example.jira_mini.exception;

public class AssigneeNotMemberException extends RuntimeException {
    public AssigneeNotMemberException(String message) {
        super(message);
    }
}