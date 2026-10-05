package com.dev.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "competitor_listings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitorListing {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @Column(
            name = "product_id",
            nullable = false
    )
    private Long productId;


    @Column(
            nullable = false,
            length = 50
    )
    private String platform;


    @Column(
            name = "competitor_name",
            nullable = false
    )
    private String competitorName;


    @Column(
            name = "external_product_id"
    )
    private String externalProductId;


    @Column(
            name = "seller_name"
    )
    private String sellerName;


    @Column(
            name = "product_url",
            nullable = false
    )
    private String productUrl;


    @Column(
            name = "alert_threshold_percent"
    )
    private BigDecimal alertThresholdPercent;


    @Column(
            name = "last_regular_price"
    )
    private BigDecimal lastRegularPrice;


    @Column(
            name = "last_sale_price"
    )
    private BigDecimal lastSalePrice;


    @Column(
            name = "last_effective_price"
    )
    private BigDecimal lastEffectivePrice;


    @Column(
            name = "last_in_stock"
    )
    private Boolean lastInStock;


    @Column(
            name = "last_crawled_at"
    )
    private LocalDateTime lastCrawledAt;


    @Column(
            name = "last_crawl_status"
    )
    private String lastCrawlStatus;


    @Column(
            name = "last_error"
    )
    private String lastError;


    @Column(
            nullable = false
    )
    private Boolean active;


    @PrePersist
    protected void onCreate() {

        if (active == null) {
            active = true;
        }

        if (alertThresholdPercent == null) {
            alertThresholdPercent =
                    BigDecimal.valueOf(5);
        }
    }
}