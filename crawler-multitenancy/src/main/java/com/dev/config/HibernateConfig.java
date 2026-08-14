package com.dev.config;

import com.dev.provider.CurrentTenantIdentifierResolverImpl;
import com.dev.provider.SchemaMultiTenantConnectionProvider;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class HibernateConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            JpaProperties jpaProperties,
            SchemaMultiTenantConnectionProvider connectionProvider,
            CurrentTenantIdentifierResolverImpl tenantResolver) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.dev.domain");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());

        // Cấu hình Multi-Tenancy Strategy per SCHEMA
        properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
        properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);
        properties.put(AvailableSettings.HBM2DDL_AUTO, "none"); // Tắt hbm2ddl để Flyway quản lý

        em.setJpaPropertyMap(properties);
        return em;
    }
}
