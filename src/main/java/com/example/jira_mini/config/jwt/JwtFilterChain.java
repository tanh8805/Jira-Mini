package com.example.jira_mini.config.jwt;

import com.example.jira_mini.service.Auth.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.*;
import java.io.*;

@Component
@RequiredArgsConstructor
public class JwtFilterChain extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final TokenBlacklistService backlistService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");
    if(authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }
    String token = authHeader.substring(7);
    try{
      if (jwtService.isTokenExpired(token)) {
        response.setContentType("application/json");
        response.setStatus(401);
        response.getWriter().write("{\"message\":\"Token expired\"}");
        return;
      }

      if (backlistService.isBlacklisted(token)) {
        response.setContentType("application/json");
        response.setStatus(401);
        response.getWriter().write("{\"message\":\"Token is blacklisted\"}");
        return;
      }
      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(role));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    catch (ExpiredJwtException ex){
      response.setContentType("application/json");
      response.setStatus(401);
      response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Access Token expired!\"}");
      return;
    }
    catch (Exception e) {
      response.setContentType("application/json");
      response.setStatus(401);
      response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Token invalid!\"}");
      return;
    }
    filterChain.doFilter(request, response);
  }
}
