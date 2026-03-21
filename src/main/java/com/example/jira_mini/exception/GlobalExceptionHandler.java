package com.example.jira_mini.exception;

import com.example.jira_mini.dto.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 409 — email đã tồn tại khi register
  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ErrorMessage> handleEmailExists(EmailAlreadyExistsException ex) {
    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorMessage.builder().status(409).message(ex.getMessage()).build());
  }

  // 401 — refresh token hết hạn
  @ExceptionHandler(TokenExpiredException.class)
  public ResponseEntity<ErrorMessage> handleTokenExpired(TokenExpiredException ex) {
    return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorMessage.builder().status(401).message(ex.getMessage()).build());
  }

  // 404 — không tìm thấy user
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorMessage> handleUserNotFound(UserNotFoundException ex) {
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorMessage.builder().status(404).message(ex.getMessage()).build());
  }

  // 404 — không tìm thấy project
  @ExceptionHandler(ProjectNotFoundException.class)
  public ResponseEntity<ErrorMessage> handleProjectNotFound(ProjectNotFoundException ex) {
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorMessage.builder().status(404).message(ex.getMessage()).build());
  }

  // 409 — user đã là member của project
  @ExceptionHandler(MemberAlreadyExistsException.class)
  public ResponseEntity<ErrorMessage> handleMemberAlreadyExists(MemberAlreadyExistsException ex) {
    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorMessage.builder().status(409).message(ex.getMessage()).build());
  }

  // 403 — không đủ quyền trong project
  @ExceptionHandler(UnauthorizedProjectAccessException.class)
  public ResponseEntity<ErrorMessage> handleUnauthorizedProjectAccess(UnauthorizedProjectAccessException ex) {
    return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorMessage.builder().status(403).message(ex.getMessage()).build());
  }

  // 500 — fallback cho mọi lỗi chưa được handle
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorMessage> handleGeneral(Exception ex) {
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorMessage.builder().status(500).message("Internal server error: " + ex.getMessage()).build());
  }
}