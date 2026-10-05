package com.dev.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class RedisConnectionCheck
        implements ApplicationRunner {

    private final StringRedisTemplate
            redisTemplate;


    @Override
    public void run(
            ApplicationArguments args
    ) {

        String response =
                redisTemplate
                        .getConnectionFactory()
                        .getConnection()
                        .ping();


        log.info(
                "Redis connection test: {}",
                response
        );
    }
}