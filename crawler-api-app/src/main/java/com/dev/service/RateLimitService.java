package com.dev.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;


    public boolean allow(
            String key,
            long maxRequests,
            Duration window
    ) {

        Long count =
                redisTemplate
                        .opsForValue()
                        .increment(
                                key
                        );


        if (
                count != null
                        &&
                        count == 1
        ) {

            redisTemplate.expire(
                    key,
                    window
            );
        }


        boolean allowed =
                count != null
                        &&
                        count <= maxRequests;


        log.info(
                "[RATE LIMIT] key={} | count={} | max={} | allowed={}",
                key,
                count,
                maxRequests,
                allowed
        );


        return allowed;
    }
}