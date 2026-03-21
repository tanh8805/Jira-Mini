package com.example.jira_mini.service.Auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
  private final RedisTemplate<String, Object> redisTemplate;

  public void blacklistToken(String token, long ttlSeconds) {
    redisTemplate.opsForValue().set("bl_" + token, "1", ttlSeconds, TimeUnit.SECONDS);
  }

  public boolean isBlacklisted(String token) {
    return redisTemplate.hasKey("bl_" + token);
  }
}
