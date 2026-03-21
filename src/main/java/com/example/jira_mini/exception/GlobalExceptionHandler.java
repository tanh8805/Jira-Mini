package com.example.jira_mini.exception;

import com.example.jira_mini.dto.ErrorMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;
import java.io.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<?> handleEmailExists(EmailAlreadyExistsException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", "Bad Request");
    errorResponse.put("message", ex.getMessage());

    return ResponseEntity.status(409).body(ErrorMessage.builder().message(ex.getMessage()).status(409).build());
  }

  @ExceptionHandler(TokenExpiredException.class)
  public ResponseEntity<?> handleTokenExpired(TokenExpiredException ex) {
    return ResponseEntity.status(401).body(ErrorMessage.builder().message(ex.getMessage()).status(401).build());
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {
    return ResponseEntity.status(404).body(ErrorMessage.builder().message(ex.getMessage()).status(404).build());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneralException(Exception ex) {
    return ResponseEntity.status(500).body(ErrorMessage.builder().message("Internal Server Error").status(500).build());
  }
}
