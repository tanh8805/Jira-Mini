package com.example.jira_mini.exception;

import java.util.*;
import java.io.*;

public class TokenExpiredException extends RuntimeException{
  public TokenExpiredException(String message){
      super(message);
  }
}
