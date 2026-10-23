package kr.gilmok.platform.tenant.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_id")
    private Long id;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "admin_email", nullable = false, unique = true, length = 100)
    private String adminEmail;

    @Column(name = "api_key_hash", nullable = false, unique = true, length = 255)
    private String apiKeyHash;

    @Column(name = "plan_tier", nullable = false, length = 20)
    private String planTier;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Tenant(String companyName, String adminEmail, String apiKeyHash, String planTier, String status) {
        this.companyName = companyName;
        this.adminEmail = adminEmail;
        this.apiKeyHash = apiKeyHash;
        this.planTier = planTier != null ? planTier : "STANDARD";
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePlanTier(String planTier) {
        this.planTier = planTier;
        this.updatedAt = LocalDateTime.now();
    }
}
