package com.dev.service;

import com.dev.domain.WebhookConfig;
import com.dev.dto.PriceAlertEvent;
import com.dev.notification.AlertSender;
import com.dev.repository.WebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final List<AlertSender> senders;

    private final WebhookConfigRepository configRepository;

    /**
     * Gửi một PriceAlertEvent tới tất cả webhook đang active.
     *
     * Một webhook lỗi sẽ không làm các webhook còn lại dừng theo.
     *
     * Nếu tất cả webhook đều lỗi -> throw exception
     * để AlertOutbox có thể retry.
     */
    public void send(
            PriceAlertEvent event
    ) {

        // =====================================================
        // 1. Load các webhook đang active
        // =====================================================

        List<WebhookConfig> configs =
                configRepository.findByIsActiveTrue();

        if (configs.isEmpty()) {

            throw new IllegalStateException(
                    "No active webhook configuration found"
            );
        }

        int successCount = 0;

        List<String> failures =
                new ArrayList<>();


        // =====================================================
        // 2. Gửi từng webhook độc lập
        // =====================================================

        for (WebhookConfig config : configs) {

            try {

                // ---------------------------------------------
                // Validate config cơ bản
                // ---------------------------------------------

                if (config.getType() == null
                        || config.getType().isBlank()) {

                    throw new IllegalArgumentException(
                            "Webhook type is required"
                    );
                }

                // ---------------------------------------------
                // Tìm sender tương ứng
                // ---------------------------------------------

                AlertSender sender =
                        findSender(
                                config.getType()
                        );

                // ---------------------------------------------
                // Gửi
                // ---------------------------------------------

                sender.send(
                        config,
                        event
                );

                successCount++;

                log.info(
                        "Webhook sent successfully. " +
                                "configId={}, name={}, type={}",
                        config.getId(),
                        config.getName(),
                        config.getType()
                );

            } catch (Exception e) {

                // Không throw ngay.
                // Tiếp tục thử config tiếp theo.

                String failureMessage =
                        "configId="
                                + config.getId()
                                + ", name="
                                + config.getName()
                                + ", type="
                                + config.getType()
                                + ", error="
                                + safeMessage(e);

                failures.add(
                        failureMessage
                );

                log.error(
                        "Webhook sending failed. {}",
                        failureMessage
                );
            }
        }


        // =====================================================
        // 3. Nếu tất cả đều thất bại
        // =====================================================

        if (successCount == 0) {

            throw new IllegalStateException(
                    "All webhook deliveries failed: "
                            + String.join(
                            " | ",
                            failures
                    )
            );
        }


        // =====================================================
        // 4. Một phần thành công, một phần thất bại
        // =====================================================

        if (!failures.isEmpty()) {

            log.warn(
                    "Price alert partially delivered. " +
                            "successCount={}, failureCount={}, failures={}",
                    successCount,
                    failures.size(),
                    failures
            );
        }
    }


    // =========================================================
    // FIND ALERT SENDER
    // =========================================================

    private AlertSender findSender(
            String type
    ) {

        if (type == null
                || type.isBlank()) {

            throw new IllegalArgumentException(
                    "Webhook type is required"
            );
        }

        return senders.stream()

                .filter(sender ->
                        sender.supports(
                                type
                        )
                )

                .findFirst()

                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unsupported webhook type: "
                                        + type
                        )
                );
    }


    // =========================================================
    // SAFE ERROR MESSAGE
    // =========================================================

    private String safeMessage(
            Exception exception
    ) {

        String message =
                exception.getMessage();

        if (message == null
                || message.isBlank()) {

            return exception
                    .getClass()
                    .getSimpleName();
        }

        return message;
    }
}