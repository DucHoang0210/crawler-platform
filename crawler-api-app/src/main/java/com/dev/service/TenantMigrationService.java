package com.dev.service;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@RequiredArgsConstructor
public class TenantMigrationService {

    private final DataSource dataSource;

    public void migrate(String schemaName) {

        validateSchemaName(schemaName);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)

                // Mỗi tenant có schema riêng
                .schemas(schemaName)
                .defaultSchema(schemaName)

                // Chỉ chạy migration của tenant
                .locations(
                        "classpath:db/migration/tenant"
                )

                .load();

        flyway.migrate();
    }

    private void validateSchemaName(String schemaName) {

        if (schemaName == null ||
                !schemaName.matches(
                        "^schema_[a-z0-9_]+$"
                )) {

            throw new IllegalArgumentException(
                    "Invalid tenant schema name"
            );
        }
    }
}