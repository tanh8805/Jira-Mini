package com.example.jira_mini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.io.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMessage {
  private String message;
}
