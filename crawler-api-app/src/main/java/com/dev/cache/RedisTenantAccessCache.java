package com.dev.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
public class RedisTenantAccessCache
        implements TenantAccessCache {

    private final StringRedisTemplate
            redisTemplate;

    private final ObjectMapper
            objectMapper;


    @Value(
            "${crawler.redis.key-prefix:crawler:local}"
    )
    private String keyPrefix;


    @Override
    public Optional<TenantAccessInfo> get(
            String username,
            String tenantId
    ) {

        try {

            String json =
                    redisTemplate
                            .opsForValue()
                            .get(
                                    buildKey(
                                            username,
                                            tenantId
                                    )
                            );


            if (
                    json == null
            ) {

                return Optional.empty();
            }


            return Optional.of(
                    objectMapper.readValue(
                            json,
                            TenantAccessInfo.class
                    )
            );

        } catch (
                Exception e
        ) {

            log.warn(
                    "Failed to read tenant access cache",
                    e
            );

            return Optional.empty();
        }
    }


    @Override
    public void put(
            String username,
            String tenantId,
            TenantAccessInfo access
    ) {

        try {

            String json =
                    objectMapper.writeValueAsString(
                            access
                    );


            redisTemplate
                    .opsForValue()
                    .set(
                            buildKey(
                                    username,
                                    tenantId
                            ),
                            json,
                            Duration.ofMinutes(
                                    5
                            )
                    );

        } catch (
                JsonProcessingException e
        ) {

            log.warn(
                    "Failed to serialize tenant access cache",
                    e
            );
        }
    }


    @Override
    public void evict(
            String username,
            String tenantId
    ) {

        redisTemplate.delete(
                buildKey(
                        username,
                        tenantId
                )
        );
    }


    private String buildKey(
            String username,
            String tenantId
    ) {

        return keyPrefix
                + ":tenant:access:"
                + username
                + ":"
                + tenantId;
    }
}