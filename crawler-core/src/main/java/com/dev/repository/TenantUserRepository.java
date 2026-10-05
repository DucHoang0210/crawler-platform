package com.dev.repository;

import com.dev.domain.TenantUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantUserRepository
        extends JpaRepository<TenantUser, Long> {

    List<TenantUser> findByUserId(Long userId);

    Optional<TenantUser> findByUserIdAndTenantId(
            Long userId,
            Long tenantId
    );

    void deleteByTenantId(Long tenantId);
}