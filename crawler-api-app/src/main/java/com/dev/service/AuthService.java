package com.dev.service;

import com.dev.domain.User;
import com.dev.domain.UserSession;
import com.dev.repository.UserRepository;
import com.dev.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final RedisSessionService redisSessionService;// Tiêm Bean thay vì new trực tiếp
    private final PasswordEncoder passwordEncoder;

    private static final long SESSION_EXPIRATION_MINUTES = 60;

    @Transactional
    public User register(String username, String email, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        String hashed = passwordEncoder.encode(rawPassword);
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(hashed)
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public String login(String usernameOrEmail, String rawPassword) {
        // 1. Tìm user
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmailIgnoreCase(usernameOrEmail))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // 2. Kiểm tra password
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(SESSION_EXPIRATION_MINUTES);
        Duration ttl = Duration.ofMinutes(SESSION_EXPIRATION_MINUTES);

        Optional<UserSession> existingSessionOpt = userSessionRepository.findByUser(user);

        String token;
        if (existingSessionOpt.isPresent()) {
            UserSession existingSession = existingSessionOpt.get();

            // Nếu session cũ còn hạn, dùng lại token cũ
            if (existingSession.getExpiresAt().isAfter(now)) {
                token = existingSession.getToken();
                // Tính lại TTL còn lại thực tế cho Redis
                ttl = Duration.between(now, existingSession.getExpiresAt());
            } else {
                // Nếu session cũ hết hạn -> Xóa token cũ khỏi Redis nếu có
                redisSessionService.delete(user.getId(), existingSession.getToken());

                // Cấp token mới
                token = UUID.randomUUID().toString();
                existingSession.setToken(token);
                existingSession.setCreatedAt(now);
                existingSession.setExpiresAt(expiresAt);
                userSessionRepository.save(existingSession);
            }
        } else {
            // Chưa có session -> Tạo mới trong DB
            token = UUID.randomUUID().toString();
            UserSession session = UserSession.builder()
                    .user(user)
                    .token(token)
                    .createdAt(now)
                    .expiresAt(expiresAt)
                    .build();
            userSessionRepository.save(session);
        }

        // 3. BẤT KỂ TRƯỜNG HỢP NÀO CŨNG PHẢI ĐẢM BẢO LƯU/DỰ PHÒNG LẠI VÀO REDIS
        redisSessionService.save(
                user.getId(),
                user.getUsername(),
                token,
                ttl
        );

        return token;
    }

    @Transactional
    public void logout(String token) {
        UserSession session = userSessionRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        redisSessionService.delete(session.getUser().getId(), session.getToken());
        userSessionRepository.delete(session);
    }

    public Optional<User> findByToken(String token) {
        return userSessionRepository
                .findByToken(token)
                .map(UserSession::getUser);
    }
}