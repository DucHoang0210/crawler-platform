package com.dev.startup;

import com.dev.repository.TenantRepository;

import com.dev.service.TenantMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantMigrationRunner
        implements ApplicationRunner {

    private final TenantRepository
            tenantRepository;

    private final TenantMigrationService
            tenantMigrationService;

    @Override
    public void run(
            ApplicationArguments args
    ) {

        tenantRepository
                .findAll()
                .forEach(
                        tenant -> {

                            if (
                                    tenant.getStatus() == null
                                            ||
                                            !"ACTIVE".equalsIgnoreCase(
                                                    tenant
                                                            .getStatus()
                                                            .toString()
                                            )
                            ) {
                                return;
                            }


                            String schemaName =
                                    tenant
                                            .getSchemaName();


                            try {

                                log.debug(
                                        "Running tenant migration. tenantId={}, schema={}",
                                        tenant.getTenantId(),
                                        schemaName
                                );


                                tenantMigrationService
                                        .migrate(
                                                schemaName
                                        );


                                log.debug(
                                        "Tenant migration completed. tenantId={}, schema={}",
                                        tenant.getTenantId(),
                                        schemaName
                                );

                            } catch (Exception e) {

                                log.error(
                                        "Tenant migration failed. tenantId={}, schema={}",
                                        tenant.getTenantId(),
                                        schemaName,
                                        e
                                );
                            }
                        }

                );
    }
}