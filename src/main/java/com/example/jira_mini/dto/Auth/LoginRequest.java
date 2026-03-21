package com.example.jira_mini.dto.Auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
  @NotBlank(message = "Email must be not blank")
  @Email
  private String email;

  @NotBlank
  @Size(min = 6,message = "Password must be at least 6 characters long")
  private String password;
}
