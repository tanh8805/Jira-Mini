package com.example.jira_mini.dto.Project;

import com.example.jira_mini.entity.enums.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AddMemberRequest {

  @Email(message = "Invalid email format")
  @NotBlank(message = "Email must not be blank")
  private String email;

  @NotNull(message = "Role must not be null")
  private ProjectRole role;
}