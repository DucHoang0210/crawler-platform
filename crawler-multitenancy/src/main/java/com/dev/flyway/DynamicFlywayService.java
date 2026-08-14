package com.dev.flyway;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Service
@Slf4j
public class DynamicFlywayService {

    private final DataSource dataSource;

    public DynamicFlywayService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Tự động khởi tạo Schema mới và nạp toàn bộ Migration SQL của Tenant
     */
    public void initTenantSchema(String schemaName) {
        log.info("Starting Dynamic Flyway Migration for Schema: {}", schemaName);

        // 1. Chạy SQL tạo Schema nếu chưa tồn tại
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName + ";");
            log.info("Schema '{}' created successfully or already exists.", schemaName);
        } catch (SQLException e) {
            log.error("Failed to create schema: {}", schemaName, e);
            throw new RuntimeException("Could not create schema: " + schemaName, e);
        }

        // 2. Chạy Flyway Migration cho Schema vừa tạo
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .locations("classpath:db/migration/tenant")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
        log.info("Dynamic Flyway Migration completed successfully for Schema: {}", schemaName);
    }
}
