package com.example.jira_mini.exception;

public class TaskNotInProjectException extends RuntimeException {
    public TaskNotInProjectException(String message) {
        super(message);
    }
}