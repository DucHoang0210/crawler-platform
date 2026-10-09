package com.dev.filter;

import com.dev.domain.User;
import com.dev.domain.UserSession;
import com.dev.repository.UserSessionRepository;
import com.dev.service.RedisSessionService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.Collections;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class AuthTokenFilter
        extends OncePerRequestFilter {

    private final UserSessionRepository
            sessionRepository;

    private final RedisSessionService
            redisSessionService;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String headerAuth =
                request.getHeader(
                        "Authorization"
                );


        if (
                headerAuth == null
                        ||
                        !headerAuth.startsWith(
                                "Bearer "
                        )
        ) {

            sendErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Missing or invalid Authorization header"
            );

            return;
        }


        String token =
                headerAuth
                        .substring(7)
                        .trim();


        if (
                token.isBlank()
        ) {

            sendErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Bearer token is empty"
            );

            return;
        }


        // =====================================================
        // 1. REDIS FIRST
        // =====================================================

        Optional<String> cachedUsername =
                redisSessionService
                        .getUsername(
                                token
                        );


        if (
                cachedUsername.isPresent()
        ) {

            authenticate(
                    cachedUsername.get()
            );


            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =====================================================
        // 2. REDIS MISS → POSTGRESQL
        // =====================================================

        Optional<UserSession> sessionOpt =
                sessionRepository
                        .findByToken(
                                token
                        );


        if (
                sessionOpt.isEmpty()
        ) {

            sendErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Invalid session token"
            );

            return;
        }


        UserSession session =
                sessionOpt.get();


        if (
                session.getExpiresAt()
                        == null
        ) {

            sendErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Session expiration is missing"
            );

            return;
        }


        LocalDateTime now =
                LocalDateTime.now();


        if (
                !session
                        .getExpiresAt()
                        .isAfter(
                                now
                        )
        ) {

            sendErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Session token has expired"
            );

            return;
        }


        User user =
                session.getUser();


        if (
                user == null
        ) {

            sendErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "User not found"
            );

            return;
        }


        // =====================================================
        // 3. REPOPULATE REDIS
        // =====================================================

        Duration remainingTtl =
                Duration.between(
                        now,
                        session.getExpiresAt()
                );


        redisSessionService.save(
                user.getId(),
                user.getUsername(),
                token,
                remainingTtl
        );


        // =====================================================
        // 4. SPRING SECURITY AUTH
        // =====================================================

        authenticate(
                user.getUsername()
        );


        filterChain.doFilter(
                request,
                response
        );
    }


    private void authenticate(
            String username
    ) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.emptyList()
                );


        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );
    }


    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String path =
                request.getRequestURI();


        String method =
                request.getMethod();


        if (
                "OPTIONS".equalsIgnoreCase(
                        method
                )
        ) {

            return true;
        }


        return path.equals(
                "/api/v1/auth/login"
        )

                || path.equals(
                "/api/v1/auth/register"
        )

                || path.equals(
                "/error"
        )

                || path.startsWith(
                "/swagger-ui"
        )

                || path.startsWith(
                "/v3/api-docs"
        )

                || path.startsWith(
                "/webjars/"
        );
    }


    private void sendErrorResponse(
            HttpServletResponse response,
            int status,
            String code,
            String message
    ) throws IOException {

        response.setStatus(
                status
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );


        response
                .getWriter()
                .write(
                        String.format(
                                """
                                {
                                  "error": "%s",
                                  "message": "%s"
                                }
                                """,
                                code,
                                message
                        )
                );
    }
}