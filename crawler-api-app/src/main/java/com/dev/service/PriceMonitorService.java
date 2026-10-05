package com.dev.service;

import com.dev.domain.CompetitorListing;
import com.dev.domain.PriceAlert;
import com.dev.domain.PriceSnapshot;
import com.dev.dto.PriceAlertEvent;
import com.dev.dto.PriceResultResponse;
import com.dev.notification.AlertPublisher;
import com.dev.repository.CompetitorListingRepository;

import com.dev.repository.PriceAlertRepository;
import com.dev.repository.PriceSnapshotRepository;
import com.dev.engine.PriceScraper;
import com.dev.engine.PriceScraperRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PriceMonitorService {

    private final CompetitorListingRepository
            competitorListingRepository;

    private final PriceSnapshotRepository
            priceSnapshotRepository;

    private final PriceAlertRepository
            priceAlertRepository;

    private final PriceScraperRegistry
            scraperRegistry;

    private final AlertPublisher alertPublisher;

    @Transactional
    public void monitor(Long listingId) {

        // =========================================
        // 1. Tìm listing
        // =========================================

        CompetitorListing listing =
                competitorListingRepository
                        .findById(listingId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Competitor listing not found: "
                                                + listingId
                                )
                        );

        if (Boolean.FALSE.equals(
                listing.getActive()
        )) {

            return;
        }

        // =========================================
        // 2. Chọn scraper
        // =========================================

        PriceScraper scraper =
                scraperRegistry.getScraper(
                        listing.getPlatform()
                );

        // =========================================
        // 3. Crawl
        // =========================================

        PriceResultResponse result;

        try {

            result = scraper.fetchPrice(
                    listing.getProductUrl()
            );

        } catch (Exception e) {

            listing.setLastCrawlStatus(
                    "FAILED"
            );

            listing.setLastError(
                    e.getMessage()
            );

            listing.setLastCrawledAt(
                    LocalDateTime.now()
            );

            competitorListingRepository.save(
                    listing
            );

            throw e;
        }

        if (result == null
                || !result.isAvailable()
                || result.getEffectivePrice() == null) {

            listing.setLastCrawlStatus(
                    "FAILED"
            );

            listing.setLastError(
                    "Price is unavailable"
            );

            listing.setLastCrawledAt(
                    LocalDateTime.now()
            );

            competitorListingRepository.save(
                    listing
            );

            return;
        }

        // =========================================
        // 4. Giá cũ và giá mới
        // =========================================

        BigDecimal oldPrice =
                listing.getLastEffectivePrice();

        BigDecimal newPrice =
                result.getEffectivePrice();

        // =========================================
        // 5. Tính biến động so với lần crawl trước
        // =========================================

        BigDecimal dropPercent =
                calculateDropPercent(
                        oldPrice,
                        newPrice
                );

        // =========================================
        // 6. Tính discount của chính listing
        // =========================================

        BigDecimal discountPercent =
                calculateDiscountPercent(
                        result.getRegularPrice(),
                        newPrice
                );

        // =========================================
        // 7. Lưu lịch sử giá
        // =========================================

        PriceSnapshot snapshot = PriceSnapshot.builder()
                        .competitorListingId(listing.getId())
                        .regularPrice(result.getRegularPrice())
                        .salePrice(result.getSalePrice())
                        .effectivePrice(newPrice)
                        .discountPercent(discountPercent)
                        .inStock(result.getInStock())
                        .promotionText(result.getPromotionText())
                        .crawledAt(LocalDateTime.now())
                        .build();

        snapshot =
                priceSnapshotRepository.save(
                        snapshot
                );

        // =========================================
        // 8. Update trạng thái listing
        // =========================================

        listing.setLastRegularPrice(
                result.getRegularPrice()
        );

        listing.setLastSalePrice(
                result.getSalePrice()
        );

        listing.setLastEffectivePrice(
                newPrice
        );

        listing.setLastInStock(
                result.getInStock()
        );

        listing.setLastCrawledAt(
                LocalDateTime.now()
        );

        listing.setLastCrawlStatus(
                "SUCCESS"
        );

        listing.setLastError(
                null
        );

        competitorListingRepository.save(
                listing
        );

        // =========================================
        // 9. Lần crawl đầu tiên
        // =========================================

        if (oldPrice == null) {

            // Chỉ ghi nhận baseline.
            // Không tạo alert.

            return;
        }

        // =========================================
        // 10. Threshold
        // =========================================

        BigDecimal threshold =
                listing
                        .getAlertThresholdPercent()
                        != null

                        ? listing
                        .getAlertThresholdPercent()

                        : BigDecimal.valueOf(5);

        // =========================================
        // 11. Không đủ điều kiện alert
        // =========================================

        if (dropPercent.compareTo(
                threshold
        ) < 0) {

            return;
        }

        // =========================================
        // 12. Tạo PriceAlert
        // =========================================

        PriceAlert alert =
                PriceAlert.builder()
                        .competitorListingId(listing.getId())
                        .priceSnapshotId(snapshot.getId())
                        .alertType("PRICE_DROP")
                        .oldPrice(oldPrice)
                        .newPrice(newPrice)
                        .changePercent(dropPercent)
                        .thresholdPercent(threshold)
                        .status("PENDING")
                        .detectedAt(LocalDateTime.now())
                        .build();

        alert = priceAlertRepository.save(alert);

// =========================================
// 13. Tạo PriceAlertEvent
// =========================================

        PriceAlertEvent event =
                PriceAlertEvent.builder()

                        .alertId(
                                alert.getId()
                        )

                        .competitorListingId(
                                listing.getId()
                        )

                        .platform(
                                listing.getPlatform()
                        )

                        .competitorName(
                                listing.getCompetitorName()
                        )

                        .oldPrice(
                                oldPrice
                        )

                        .newPrice(
                                newPrice
                        )

                        .dropPercent(
                                dropPercent
                        )

                        .observedAt(
                                LocalDateTime.now()
                        )

                        .build();

// =========================================
// 14. Publish vào alert_outbox
// =========================================

        alertPublisher.publish(
                event
        );

    }

    // =====================================================
    // PRICE DROP
    // =====================================================

    private BigDecimal calculateDropPercent(
            BigDecimal oldPrice,
            BigDecimal newPrice
    ) {

        if (oldPrice == null
                || newPrice == null
                || oldPrice.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            return BigDecimal.ZERO;
        }

        // Giá không giảm
        if (newPrice.compareTo(
                oldPrice
        ) >= 0) {

            return BigDecimal.ZERO;
        }

        return oldPrice
                .subtract(newPrice)
                .divide(
                        oldPrice,
                        4,
                        RoundingMode.HALF_UP
                )
                .multiply(
                        BigDecimal.valueOf(100)
                );
    }

    // =====================================================
    // LISTING DISCOUNT
    // =====================================================

    private BigDecimal calculateDiscountPercent(
            BigDecimal regularPrice,
            BigDecimal effectivePrice
    ) {

        if (regularPrice == null
                || effectivePrice == null
                || regularPrice.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            return BigDecimal.ZERO;
        }

        if (effectivePrice.compareTo(
                regularPrice
        ) >= 0) {

            return BigDecimal.ZERO;
        }

        return regularPrice
                .subtract(effectivePrice)
                .divide(
                        regularPrice,
                        4,
                        RoundingMode.HALF_UP
                )
                .multiply(
                        BigDecimal.valueOf(100)
                );
    }
}