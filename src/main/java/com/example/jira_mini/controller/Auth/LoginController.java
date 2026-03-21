package com.example.jira_mini.controller.Auth;

import com.example.jira_mini.config.jwt.JwtService;
import com.example.jira_mini.dto.Auth.LoginRequest;
import com.example.jira_mini.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
    String email = loginRequest.getEmail();
    String password = loginRequest.getPassword();
    Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String accessToken = jwtService.generateToken(authentication);
    String refreshToken = jwtService.generateRefreshToken(authentication);
    return ResponseEntity.status(200).body(TokenResponse.builder().message("Login Successful").accessToken(accessToken).refreshToken(refreshToken).build());
  }
}
