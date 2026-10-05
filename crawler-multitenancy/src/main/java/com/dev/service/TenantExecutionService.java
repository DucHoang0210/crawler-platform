package com.dev.service;

import com.dev.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class TenantExecutionService {

    private final JdbcTemplate jdbcTemplate;

    // =========================================================
    // 1. Execute action KHÔNG cần return
    // =========================================================
    @Transactional
    public void execute(
            String schemaName,
            Runnable action
    ) {

        validateSchema(schemaName);

        TenantContext.setCurrentTenant(
                schemaName
        );

        try {

            jdbcTemplate.execute(
                    "SET LOCAL search_path TO "
                            + schemaName
                            + ", public"
            );

            action.run();

        } finally {

            TenantContext.clear();
        }
    }

    // =========================================================
    // 2. Execute action CÓ return
    // =========================================================
    @Transactional
    public <T> T execute(
            String schemaName,
            Supplier<T> action
    ) {

        validateSchema(schemaName);

        TenantContext.setCurrentTenant(
                schemaName
        );

        try {

            jdbcTemplate.execute(
                    "SET LOCAL search_path TO "
                            + schemaName
                            + ", public"
            );

            return action.get();

        } finally {

            TenantContext.clear();
        }
    }

    // =========================================================
    // Validate schema
    // =========================================================
    private void validateSchema(
            String schemaName
    ) {

        if (
                schemaName == null
                        || !schemaName.matches(
                        "^schema_[a-z0-9_]+$"
                )
        ) {

            throw new IllegalArgumentException(
                    "Invalid tenant schema: "
                            + schemaName
            );
        }
    }
}