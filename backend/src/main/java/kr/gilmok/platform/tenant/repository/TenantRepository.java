package kr.gilmok.platform.tenant.repository;

import kr.gilmok.platform.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByApiKeyHash(String apiKeyHash);
    Optional<Tenant> findByAdminEmail(String adminEmail);
}
