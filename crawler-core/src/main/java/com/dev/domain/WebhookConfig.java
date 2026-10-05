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

    // TELEGRAM / ERP / ZALO
    @Column(name = "type", nullable = false, length = 30)
    private String type;

    // Tên cấu hình, ví dụ "Main Telegram Bot"
    @Column(name = "name", length = 100)
    private String name;

    // Dùng cho ERP/Zalo webhook
    @Column(name = "target_url", length = 1000)
    private String targetUrl;

    // Token bot / API key
    @Column(name = "secret_key")
    private String secretKey;

    // Telegram chat id hoặc destination khác
    @Column(name = "destination", length = 255)
    private String destination;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}