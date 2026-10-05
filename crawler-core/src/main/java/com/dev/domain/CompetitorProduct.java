package com.dev.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "competitor_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitorProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;

    @Column(name = "product_name")
    private String productName;

    private String source;

    @Column(name = "product_url")
    private String productUrl;

    @Column(name = "current_price")
    private BigDecimal currentPrice;

    @Column(name = "alert_threshold")
    private BigDecimal alertThreshold;

    private Boolean active;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;
}