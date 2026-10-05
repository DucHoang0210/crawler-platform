package com.dev.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.data.redis.core.script.DefaultRedisScript;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PriceMonitorLockService {

    private final StringRedisTemplate
            redisTemplate;


    @Value(
            "${crawler.redis.key-prefix:crawler:local}"
    )
    private String keyPrefix;


    private static final DefaultRedisScript<Long>
            UNLOCK_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    if redis.call('get', KEYS[1]) == ARGV[1]
                    then
                        return redis.call('del', KEYS[1])
                    else
                        return 0
                    end
                    """,
                    Long.class
            );


    // =========================================================
    // TRY LOCK
    // =========================================================

    public String tryLock(
            String schemaName,
            Long listingId
    ) {

        String key =
                buildKey(
                        schemaName,
                        listingId
                );


        String lockToken =
                UUID.randomUUID()
                        .toString();


        Boolean acquired =
                redisTemplate
                        .opsForValue()
                        .setIfAbsent(
                                key,
                                lockToken,
                                Duration.ofMinutes(
                                        2
                                )
                        );


        if (
                !Boolean.TRUE.equals(
                        acquired
                )
        ) {

            return null;
        }


        return lockToken;
    }


    // =========================================================
    // UNLOCK
    // =========================================================

    public void unlock(
            String schemaName,
            Long listingId,
            String lockToken
    ) {

        redisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(
                        buildKey(
                                schemaName,
                                listingId
                        )
                ),
                lockToken
        );
    }


    private String buildKey(
            String schemaName,
            Long listingId
    ) {

        return keyPrefix
                + ":lock:price-monitor:"
                + schemaName
                + ":"
                + listingId;
    }
}