package com.example.jira_mini.config.security;

import com.example.jira_mini.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;
import java.io.*;

@RequiredArgsConstructor
public class CustomerDetailsService implements UserDetailsService {
  private final UserRepository userRepository;
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByEmail(username)
            .map((user) -> new CustomerDetails(user))
            .orElseThrow(() -> new UsernameNotFoundException(username));
  }
}
