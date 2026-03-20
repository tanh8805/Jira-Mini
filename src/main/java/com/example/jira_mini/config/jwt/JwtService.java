package com.example.jira_mini.config.jwt;

import com.example.jira_mini.config.security.CustomerDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
  @Value("${jwt.secret}")
  private String JWT_SECRET;

  @Value("${jwt.expiration}")
  private long JWT_EXPIRATION;

  public String generateToken(Authentication authentication) {
    CustomerDetails userDetails = (CustomerDetails) authentication.getPrincipal();
    if(userDetails == null) {
      throw new IllegalArgumentException("Invalid username or password");
    }
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", authentication.getAuthorities());
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUser().getEmail())
            .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes()), SignatureAlgorithm.HS256)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
            .compact();
  }
  public String extractEmail(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET.getBytes()))
            .build()
            .parseClaimsJws(token).getBody().getSubject();
  }
  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody();
  }
  private Date extractExpiration(String token) {
    return extractAllClaims(token).getExpiration();
  }
  public boolean isTokenExpired(String token) {
    try {
      return extractExpiration(token).before(new Date());
    } catch (Exception e) {
      return true;
    }
  }
  public String extractRole(String token) {
    return extractAllClaims(token).get("role").toString();
  }
}
