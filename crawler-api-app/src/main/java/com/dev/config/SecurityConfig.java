package com.dev.config;

import com.dev.filter.AuthTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthTokenFilter authTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // =====================================================
                // CORS
                // =====================================================
                // Sử dụng CorsConfigurationSource bean
                // được khai báo trong CorsConfig.java
                // =====================================================

                .cors(
                        Customizer.withDefaults()
                )


                // =====================================================
                // CSRF
                // =====================================================
                // REST API dùng Bearer Token,
                // không dùng browser session/cookie authentication.
                // =====================================================

                .csrf(
                        csrf -> csrf.disable()
                )


                // =====================================================
                // SESSION
                // =====================================================
                // Không sử dụng HTTP session của Spring Security.
                // Authentication được xác định từ Bearer Token
                // trong mỗi request.
                // =====================================================

                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )


                // =====================================================
                // AUTHORIZATION
                // =====================================================

                .authorizeHttpRequests(
                        auth -> auth

                                // -------------------------------------
                                // CORS preflight
                                // -------------------------------------

                                .requestMatchers(
                                        HttpMethod.OPTIONS,
                                        "/**"
                                )
                                .permitAll()


                                // -------------------------------------
                                // Public authentication APIs
                                // -------------------------------------

                                .requestMatchers(
                                        "/api/v1/auth/login",
                                        "/api/v1/auth/register"
                                )
                                .permitAll()


                                // -------------------------------------
                                // Swagger / OpenAPI
                                // -------------------------------------

                                .requestMatchers(
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",

                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/v3/api-docs.yaml",

                                        "/webjars/**"
                                )
                                .permitAll()


                                // -------------------------------------
                                // Spring error endpoint
                                // -------------------------------------
                                // Cho phép error endpoint để tránh
                                // lỗi thật bị biến thành 401/403.
                                // -------------------------------------

                                .requestMatchers(
                                        "/error"
                                )
                                .permitAll()


                                // -------------------------------------
                                // Everything else requires authentication
                                // -------------------------------------

                                .anyRequest()
                                .authenticated()
                )


                // =====================================================
                // DISABLE DEFAULT LOGIN
                // =====================================================

                .formLogin(
                        form -> form.disable()
                )

                .httpBasic(
                        basic -> basic.disable()
                )


                // =====================================================
                // CUSTOM BEARER TOKEN FILTER
                // =====================================================
                // AuthTokenFilter chạy trước Spring's
                // UsernamePasswordAuthenticationFilter.
                // =====================================================

                .addFilterBefore(
                        authTokenFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}