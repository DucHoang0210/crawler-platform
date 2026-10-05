package com.dev.cache;

import java.util.Optional;


public interface TenantAccessCache {

    Optional<TenantAccessInfo> get(
            String username,
            String tenantId
    );


    void put(
            String username,
            String tenantId,
            TenantAccessInfo access
    );


    void evict(
            String username,
            String tenantId
    );
}