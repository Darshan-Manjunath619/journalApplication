package com.darshan.journalApplication.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisService(RedisTemplate<String, String> redisTemplate,
                        ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> T get(String key , Class<T> entityClass){
        try{
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) return null;
            return objectMapper.readValue(value,entityClass);

        } catch (Exception e) {
            log.warn("Redis read failed for key {}", key, e);
            return null;
        }
    }

    public void set(String key, Object o, Long ttl){
        try{
            String s = objectMapper.writeValueAsString(o);
            redisTemplate.opsForValue().set(key,s,ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis write failed for key {}", key, e);
        }
    }
}
