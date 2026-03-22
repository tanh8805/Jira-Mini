package com.example.jira_mini.controller.Auth;

import com.example.jira_mini.config.jwt.JwtService;
import com.example.jira_mini.dto.ResponseMessage;
import com.example.jira_mini.service.Auth.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Quản lý Auth")
public class LogoutController {
  private final TokenBlacklistService blacklistService;
  private final JwtService jwtService;

  @PostMapping("/logout")
  @Operation(summary = "Đăng xuất")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Đăng xuất thành công"),
          @ApiResponse(responseCode = "400", description = "Không có token")
  })
  public ResponseEntity<?> logout(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");

    if(authHeader == null || !authHeader.startsWith("Bearer ")) {
      return ResponseEntity.badRequest().body(ResponseMessage.builder().message("No token!").build());
    }

    String token = authHeader.substring(7);

    Date exp = jwtService.extractExpiration(token);
    long ttl = (exp.getTime() - System.currentTimeMillis()) / 1000;

    if(ttl > 0) {
      blacklistService.blacklistToken(token, ttl);
    }

    return ResponseEntity.ok(ResponseMessage.builder().message("Logout success!").build());
  }
}
