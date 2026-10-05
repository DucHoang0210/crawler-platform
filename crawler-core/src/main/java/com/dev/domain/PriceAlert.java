package com.dev.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_alerts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlert {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @Column(
            name = "competitor_listing_id",
            nullable = false
    )
    private Long competitorListingId;


    @Column(
            name = "price_snapshot_id",
            nullable = false
    )
    private Long priceSnapshotId;


    @Column(
            name = "alert_type",
            nullable = false,
            length = 50
    )
    private String alertType;


    @Column(
            name = "old_price",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal oldPrice;


    @Column(
            name = "new_price",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal newPrice;


    @Column(
            name = "change_percent",
            nullable = false,
            precision = 10,
            scale = 4
    )
    private BigDecimal changePercent;


    @Column(
            name = "threshold_percent",
            nullable = false,
            precision = 10,
            scale = 4
    )
    private BigDecimal thresholdPercent;


    @Column(
            name = "status",
            nullable = false,
            length = 50
    )
    private String status;


    @Column(
            name = "detected_at",
            nullable = false
    )
    private LocalDateTime detectedAt;


    @PrePersist
    protected void onCreate() {

        if (alertType == null) {
            alertType = "PRICE_DROP";
        }

        if (status == null) {
            status = "PENDING";
        }

        if (detectedAt == null) {
            detectedAt =
                    LocalDateTime.now();
        }
    }
}