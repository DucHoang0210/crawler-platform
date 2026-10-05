package com.dev.repository;

import com.dev.domain.WebhookConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookConfigRepository
        extends JpaRepository<WebhookConfig, Long> {

    List<WebhookConfig> findByIsActiveTrue();
}