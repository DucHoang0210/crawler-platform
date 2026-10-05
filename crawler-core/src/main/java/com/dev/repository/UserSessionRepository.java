package com.dev.repository;

import com.dev.domain.User;
import com.dev.domain.UserSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    @EntityGraph(attributePaths = {"user"})
    Optional<UserSession> findByToken(String token);
    Optional<UserSession> findByUser(User user);
    void deleteByToken(String token);
}
