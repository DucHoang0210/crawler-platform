package com.dev.provider;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

@Component
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;
    // Chống SQL Injection: Chỉ cho phép tên Schema chứa chữ cái, số và dấu gạch dưới
    private static final Pattern VALID_SCHEMA_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        if (tenantIdentifier != null && !tenantIdentifier.isBlank()) {
            if (!VALID_SCHEMA_PATTERN.matcher(tenantIdentifier).matches()) {
                connection.close();
                throw new IllegalArgumentException("Invalid Schema Name: " + tenantIdentifier);
            }
            // Chuyển kết nối sang Schema của Tenant
            connection.createStatement().execute("SET search_path TO " + tenantIdentifier);
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try {
            connection.createStatement().execute("SET search_path TO public");
        } finally {
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return true;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}
