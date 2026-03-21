package com.example.jira_mini.config.jwt;

import com.example.jira_mini.config.security.CustomerDetails;
import com.example.jira_mini.entity.User;
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

  @Value("${jwt.expiration-refresh-token}")
  private long JWT_EXPIRATION_REFRESH;

  @Value("${jwt.secret_refresh_token}")
  private String JWT_SECRET_REFRESH;

  public String generateToken(Authentication authentication) {
    CustomerDetails userDetails = (CustomerDetails) authentication.getPrincipal();
    if(userDetails == null) {
      throw new IllegalArgumentException("Invalid username or password");
    }
    Map<String, Object> claims = new HashMap<>();
    String role = authentication.getAuthorities().iterator().next().getAuthority();
    return Jwts.builder()
            .claim("role", role)
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
  public Date extractExpiration(String token) {
    return extractAllClaims(token).getExpiration();
  }
  public boolean isTokenExpired(String token) {
    try {
      return extractExpiration(token).before(new Date());
    } catch (Exception e) {
      return true;
    }
  }
  //REFRESH TOKEN

  public String generateRefreshToken(Authentication authentication) {
    CustomerDetails userDetails = (CustomerDetails) authentication.getPrincipal();
    if(userDetails == null) {
      throw new IllegalArgumentException("Invalid username or password");
    }
    return Jwts.builder()
            .setSubject(userDetails.getUser().getId().toString())
            .signWith(Keys.hmacShaKeyFor(JWT_SECRET_REFRESH.getBytes()), SignatureAlgorithm.HS256)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_REFRESH))
            .compact();
  }

  public String extractRole(String token) {
    return extractAllClaims(token).get("role").toString();
  }
  private Claims extractRefreshClaims(String refreshToken){
    return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET_REFRESH.getBytes()))
            .build()
            .parseClaimsJws(refreshToken).getBody();
  }

  public Date extractExpirationRefreshToken(String token) {
    return extractRefreshClaims(token).getExpiration();
  }
  public boolean isRefreshTokenExpired(String token) {
    try {
      return extractExpiration(token).before(new Date());
    } catch (Exception e) {
      return true;
    }
  }
  public String extractUserIdFromRefreshToken(String token) {
    return extractAllClaims(token).getSubject();
  }
  public String generateAccessTokenFromUser(String email, String role) {
    return Jwts.builder()
            .claim("role", role)
            .setSubject(email)
            .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes()), SignatureAlgorithm.HS256)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
            .compact();
  }
}
