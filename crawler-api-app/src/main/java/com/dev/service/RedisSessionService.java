package com.dev.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.dao.DataAccessException;

import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSessionService {

    private final StringRedisTemplate
            redisTemplate;


    @Value(
            "${crawler.redis.key-prefix:crawler:local}"
    )
    private String keyPrefix;


    // =========================================================
    // SAVE SESSION
    // =========================================================

    public void save(
            Long userId,
            String username,
            String token,
            Duration ttl
    ) {

        if (
                ttl == null
                        ||
                        ttl.isNegative()
                        ||
                        ttl.isZero()
        ) {
            return;
        }


        try {

            redisTemplate
                    .opsForValue()
                    .set(
                            sessionKey(
                                    token
                            ),
                            username,
                            ttl
                    );


            /*
             * Map user -> current token.
             *
             * Sau này dùng để đảm bảo
             * 1 user chỉ có 1 active session.
             */
            redisTemplate
                    .opsForValue()
                    .set(
                            userTokenKey(
                                    userId
                            ),
                            token,
                            ttl
                    );

        } catch (
                DataAccessException e
        ) {

            log.warn(
                    "Failed to cache authentication session in Redis. userId={}",
                    userId,
                    e
            );
        }
    }


    // =========================================================
    // GET USERNAME FROM TOKEN
    // =========================================================

    public Optional<String> getUsername(
            String token
    ) {

        try {

            String username =
                    redisTemplate
                            .opsForValue()
                            .get(
                                    sessionKey(
                                            token
                                    )
                            );


            return Optional.ofNullable(
                    username
            );

        } catch (
                DataAccessException e
        ) {

            /*
             * Redis lỗi thì return MISS.
             *
             * AuthTokenFilter sẽ fallback PostgreSQL.
             */
            log.warn(
                    "Redis session lookup failed",
                    e
            );

            return Optional.empty();
        }
    }


    // =========================================================
    // GET ACTIVE TOKEN OF USER
    // =========================================================

    public Optional<String> getActiveToken(
            Long userId
    ) {

        try {

            String token =
                    redisTemplate
                            .opsForValue()
                            .get(
                                    userTokenKey(
                                            userId
                                    )
                            );


            if (
                    token == null
            ) {

                return Optional.empty();
            }


            Boolean sessionExists =
                    redisTemplate
                            .hasKey(
                                    sessionKey(
                                            token
                                    )
                            );


            if (
                    Boolean.TRUE.equals(
                            sessionExists
                    )
            ) {

                return Optional.of(
                        token
                );
            }


            /*
             * Pointer bị stale.
             */
            redisTemplate.delete(
                    userTokenKey(
                            userId
                    )
            );


            return Optional.empty();

        } catch (
                DataAccessException e
        ) {

            log.warn(
                    "Redis active-session lookup failed. userId={}",
                    userId,
                    e
            );

            return Optional.empty();
        }
    }


    // =========================================================
    // DELETE SESSION
    // =========================================================

    public void delete(
            Long userId,
            String token
    ) {

        try {

            redisTemplate.delete(
                    sessionKey(
                            token
                    )
            );


            String activeToken =
                    redisTemplate
                            .opsForValue()
                            .get(
                                    userTokenKey(
                                            userId
                                    )
                            );


            if (
                    token.equals(
                            activeToken
                    )
            ) {

                redisTemplate.delete(
                        userTokenKey(
                                userId
                        )
                );
            }

        } catch (
                DataAccessException e
        ) {

            log.warn(
                    "Failed to delete Redis session. userId={}",
                    userId,
                    e
            );
        }
    }


    // =========================================================
    // KEYS
    // =========================================================

    private String sessionKey(
            String token
    ) {

        return keyPrefix
                + ":auth:session:"
                + token;
    }


    private String userTokenKey(
            Long userId
    ) {

        return keyPrefix
                + ":auth:user:"
                + userId
                + ":token";
    }
}