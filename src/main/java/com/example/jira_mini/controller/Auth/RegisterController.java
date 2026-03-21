package com.example.jira_mini.controller.Auth;

import com.example.jira_mini.dto.Auth.RegisterRequest;
import com.example.jira_mini.dto.ResponseMessage;
import com.example.jira_mini.entity.User;
import com.example.jira_mini.repository.UserRepository;
import com.example.jira_mini.service.Auth.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegisterController {
  private final RegisterService registerService;
  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
    String email = registerRequest.getEmail();
    String password = registerRequest.getPassword();
    String fullName = registerRequest.getFullName();
    registerService.register(email, password, fullName);
    return ResponseEntity.ok().body(ResponseMessage.builder().message("Register successfully!").build());
  }
}
