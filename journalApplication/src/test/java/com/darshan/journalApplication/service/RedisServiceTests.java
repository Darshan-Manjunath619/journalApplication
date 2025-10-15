package com.darshan.journalApplication.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Component
@SpringBootTest
public class RedisServiceTests {

    @Autowired
    private RedisTemplate redisTemplate;

        @Test
        void sendMailTests(){
            //redisTemplate.opsForValue().set("email","gmail@email.com");
            Object email = redisTemplate.opsForValue().get("email");
        }
    }

