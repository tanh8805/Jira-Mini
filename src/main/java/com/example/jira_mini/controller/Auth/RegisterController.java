package com.example.jira_mini.controller.Auth;

import com.example.jira_mini.dto.Auth.RegisterRequest;
import com.example.jira_mini.dto.ResponseMessage;
import com.example.jira_mini.entity.User;
import com.example.jira_mini.repository.UserRepository;
import com.example.jira_mini.service.Auth.RegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "Quản lý Auth")
public class RegisterController {
  private final RegisterService registerService;
  @PostMapping("/register")
  @Operation(summary = "Đăng kí")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Đăng kí thành công"),
          @ApiResponse(responseCode = "400", description = "Validation lỗi (blank, sai format...)"),
          @ApiResponse(responseCode = "409", description = "Email đã tồn tại")
  })
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
    String email = registerRequest.getEmail();
    String password = registerRequest.getPassword();
    String fullName = registerRequest.getFullName();
    registerService.register(email, password, fullName);
    return ResponseEntity.ok().body(ResponseMessage.builder().message("Register successfully!").build());
  }
}
