package com.dev.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "demo_products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoProduct {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "public_id",
            nullable = false,
            unique = true
    )
    private UUID publicId;

    @Column(
            nullable = false
    )
    private String name;

    @Column(
            name = "regular_price",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal regularPrice;

    @Column(
            name = "current_price",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal currentPrice;

    @Column(
            name = "in_stock",
            nullable = false
    )
    private Boolean inStock;

    @Column(
            name = "promotion_text"
    )
    private String promotionText;

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        if (publicId == null) {
            publicId =
                    UUID.randomUUID();
        }

        if (inStock == null) {
            inStock = true;
        }

        OffsetDateTime now =
                OffsetDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt =
                OffsetDateTime.now();
    }
}