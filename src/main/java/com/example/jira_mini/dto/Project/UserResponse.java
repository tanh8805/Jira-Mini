package com.example.jira_mini.dto.Project;

import com.example.jira_mini.entity.enums.SystemRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.*;
import java.io.*;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
  private UUID id;
  private String email;
  private String fullName;
  private SystemRole role;
}
