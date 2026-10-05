package com.dev.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_snapshots")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "competitor_listing_id", nullable = false)
    private Long competitorListingId;

    @Column(name = "regular_price")
    private BigDecimal regularPrice;

    @Column(name = "sale_price")
    private BigDecimal salePrice;

    @Column(name = "effective_price", nullable = false)
    private BigDecimal effectivePrice;

    @Column(name = "discount_percent")
    private BigDecimal discountPercent;

    @Column(name = "in_stock")
    private Boolean inStock;

    @Column(name = "promotion_text")
    private String promotionText;

    @Column(name = "crawled_at")
    private LocalDateTime crawledAt;
}