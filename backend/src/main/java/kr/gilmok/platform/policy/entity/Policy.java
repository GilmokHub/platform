package kr.gilmok.platform.policy.entity;

import jakarta.persistence.*;
import kr.gilmok.platform.policy.constants.PolicyDefaults;
import kr.gilmok.platform.policy.vo.BlockRules;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "queue_policies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long id;

    @Column(name = "queue_id", nullable = false, unique = true)
    private Long queueId;

    @Version
    @Column(name = "policy_version", nullable = false)
    private long policyVersion;

    @Column(name = "admission_rps", nullable = false)
    private int admissionRps;

    @Column(name = "admission_concurrency", nullable = false)
    private int admissionConcurrency;

    @Column(name = "max_requests_per_second", nullable = false)
    private int maxRequestsPerSecond = PolicyDefaults.MAX_REQUESTS_PER_SECOND;

    @Column(name = "block_duration_minutes", nullable = false)
    private int blockDurationMinutes = PolicyDefaults.BLOCK_DURATION_MINUTES;

    @Column(name = "gate_mode", length = 20)
    private String gateMode = PolicyDefaults.GATE_MODE;

    @Convert(converter = BlockRulesConverter.class)
    @Column(name = "block_rules", columnDefinition = "TEXT")
    private BlockRules blockRules;

    @Column(name = "token_ttl_seconds", nullable = false)
    private int tokenTtlSeconds = 300;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;

    @Column(name = "updated_by_username", length = 50)
    private String updatedByUsername;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Policy(Long queueId) {
        this.queueId = queueId;
        this.policyVersion = 1L;
        this.admissionRps = PolicyDefaults.ADMISSION_RPS;
        this.admissionConcurrency = PolicyDefaults.ADMISSION_CONCURRENCY;
        this.blockRules = PolicyDefaults.blockRules();
        this.tokenTtlSeconds = 300;
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public Policy(Long queueId, int admissionRps, int admissionConcurrency,
                  int maxRequestsPerSecond, int blockDurationMinutes,
                  String gateMode, BlockRules blockRules, int tokenTtlSeconds,
                  Long updatedByUserId, String updatedByUsername) {
        this.queueId = queueId;
        this.policyVersion = 1L;
        this.admissionRps = admissionRps;
        this.admissionConcurrency = admissionConcurrency;
        this.maxRequestsPerSecond = maxRequestsPerSecond;
        this.blockDurationMinutes = blockDurationMinutes;
        this.gateMode = gateMode != null ? gateMode : PolicyDefaults.GATE_MODE;
        this.blockRules = blockRules != null ? blockRules : PolicyDefaults.blockRules();
        this.tokenTtlSeconds = tokenTtlSeconds > 0 ? tokenTtlSeconds : 300;
        this.updatedByUserId = updatedByUserId;
        this.updatedByUsername = updatedByUsername;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getEventId() {
        return this.queueId;
    }

    public void updatePolicy(int rps, int concurrency, BlockRules rules,
                            String gateMode, Long updatedByUserId, String updatedByUsername,
                            Integer maxRequestsPerSecond, Integer blockDurationMinutes) {
        updatePolicy(rps, concurrency, rules, gateMode, updatedByUserId, updatedByUsername,
                maxRequestsPerSecond, blockDurationMinutes, null);
    }

    public void updatePolicy(int rps, int concurrency, BlockRules rules,
                            String gateMode, Long updatedByUserId, String updatedByUsername,
                            Integer maxRequestsPerSecond, Integer blockDurationMinutes,
                            Integer tokenTtlSeconds) {
        this.admissionRps = rps;
        this.admissionConcurrency = concurrency;
        this.blockRules = rules != null ? rules : BlockRules.empty();
        if (gateMode != null && !gateMode.isBlank()) {
            this.gateMode = gateMode;
        }
        this.updatedByUserId = updatedByUserId;
        this.updatedByUsername = updatedByUsername;
        if (maxRequestsPerSecond != null && maxRequestsPerSecond >= 0) {
            this.maxRequestsPerSecond = maxRequestsPerSecond;
        }
        if (blockDurationMinutes != null && blockDurationMinutes >= 0) {
            this.blockDurationMinutes = blockDurationMinutes;
        }
        if (tokenTtlSeconds != null && tokenTtlSeconds > 0) {
            this.tokenTtlSeconds = tokenTtlSeconds;
        }
    }

    public void applyFromHistory(PolicyHistory history, Long rollbackByUserId, String rollbackByUsername) {
        this.admissionRps = history.getAdmissionRps();
        this.admissionConcurrency = history.getAdmissionConcurrency();
        this.blockRules = history.getBlockRules() != null ? history.getBlockRules() : BlockRules.empty();
        if (history.getGateMode() != null && !history.getGateMode().isBlank()) {
            this.gateMode = history.getGateMode();
        }
        this.maxRequestsPerSecond = history.getMaxRequestsPerSecond();
        this.blockDurationMinutes = history.getBlockDurationMinutes();
        this.tokenTtlSeconds = history.getTokenTtlSeconds();
        this.updatedByUserId = rollbackByUserId;
        this.updatedByUsername = rollbackByUsername;
    }
}
