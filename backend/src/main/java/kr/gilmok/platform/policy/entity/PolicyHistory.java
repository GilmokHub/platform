package kr.gilmok.platform.policy.entity;

import jakarta.persistence.*;
import kr.gilmok.platform.policy.vo.BlockRules;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "policy_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PolicyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @Column(name = "queue_id", nullable = false)
    private Long queueId;

    @Column(name = "policy_version", nullable = false)
    private long policyVersion;

    @Column(name = "admission_rps", nullable = false)
    private int admissionRps;

    @Column(name = "admission_concurrency", nullable = false)
    private int admissionConcurrency;

    @Column(name = "max_requests_per_second", nullable = false)
    private int maxRequestsPerSecond;

    @Column(name = "block_duration_minutes", nullable = false)
    private int blockDurationMinutes;

    @Column(name = "gate_mode", length = 20)
    private String gateMode;

    @Convert(converter = BlockRulesConverter.class)
    @Column(name = "block_rules", columnDefinition = "TEXT")
    private BlockRules blockRules;

    @Column(name = "token_ttl_seconds", nullable = false)
    @Builder.Default
    private int tokenTtlSeconds = 300;

    @Column(name = "trigger_type", nullable = false, length = 20)
    @Builder.Default
    private String triggerType = "MANUAL";

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;

    @Column(name = "updated_by_username", length = 50)
    private String updatedByUsername;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Long getEventId() {
        return this.queueId;
    }

    public static PolicyHistory from(Policy policy) {
        return from(policy, "MANUAL");
    }

    public static PolicyHistory from(Policy policy, String triggerType) {
        return PolicyHistory.builder()
                .queueId(policy.getQueueId())
                .policyVersion(policy.getPolicyVersion())
                .admissionRps(policy.getAdmissionRps())
                .admissionConcurrency(policy.getAdmissionConcurrency())
                .maxRequestsPerSecond(policy.getMaxRequestsPerSecond())
                .blockDurationMinutes(policy.getBlockDurationMinutes())
                .gateMode(policy.getGateMode())
                .blockRules(policy.getBlockRules())
                .tokenTtlSeconds(policy.getTokenTtlSeconds())
                .triggerType(triggerType != null ? triggerType : "MANUAL")
                .updatedByUserId(policy.getUpdatedByUserId())
                .updatedByUsername(policy.getUpdatedByUsername())
                .build();
    }
}