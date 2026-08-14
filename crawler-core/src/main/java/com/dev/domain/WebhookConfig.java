package com.dev.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "webhook_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_url", nullable = false, length = 1000)
    private String targetUrl;

    @Column(name = "secret_key", nullable = false)
    private String secretKey;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
