package com.example.jira_mini.controller.Auth;

import com.example.jira_mini.config.jwt.JwtService;
import com.example.jira_mini.dto.TokenResponse;
import com.example.jira_mini.entity.User;
import com.example.jira_mini.exception.TokenExpiredException;
import com.example.jira_mini.exception.UserNotFoundException;
import com.example.jira_mini.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RefreshController {
  private final JwtService jwtService;
  private final UserRepository userRepository;

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new TokenExpiredException("Refresh token missing");
    }

    String refreshToken = authHeader.substring(7);

    if (jwtService.isRefreshTokenExpired(refreshToken)) {
      throw new TokenExpiredException("Refresh token expired");
    }

    String userIdString = jwtService.extractUserIdFromRefreshToken(refreshToken);
    User user = userRepository.findById(UUID.fromString(userIdString))
            .orElseThrow(() -> new UserNotFoundException("User Not Found!"));

    String accessToken = jwtService.generateAccessTokenFromUser(
            user.getEmail(),
            "ROLE_" + user.getRole()
    );

    return ResponseEntity.ok(TokenResponse.builder().accessToken(accessToken).build());
  }
}
