package com.dahye.wms.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

  @Bean(name = "certCodeRedisTemplate")
  public RedisTemplate<String, String> certCodeRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }

  @Bean(name = "checkVerifyEmailRedisTemplate")
  public RedisTemplate<String, Boolean> checkVerifyEmailRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Boolean> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }
}