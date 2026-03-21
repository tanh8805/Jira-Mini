package com.example.jira_mini.exception;

import java.util.*;
import java.io.*;

public class UserNotFoundException extends RuntimeException{
  public UserNotFoundException(String message) {
      super(message);
  }
}
