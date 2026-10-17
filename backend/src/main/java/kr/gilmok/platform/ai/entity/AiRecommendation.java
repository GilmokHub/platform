package kr.gilmok.platform.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_recommendations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long id;

    @Column(name = "queue_id", nullable = false)
    private Long queueId;

    @Column(name = "applied", nullable = false)
    private Boolean applied = false;

    @Column(name = "applied_policy_version")
    private Integer appliedPolicyVersion;

    @Column(name = "input_window_sec", nullable = false)
    private Integer inputWindowSec;

    @Column(name = "metrics_snapshot_json", columnDefinition = "TEXT", nullable = false)
    private String metricsSnapshotJson;

    @Column(name = "recommended_policy_json", columnDefinition = "TEXT", nullable = false)
    private String recommendedPolicyJson;

    @Column(name = "rationale", columnDefinition = "TEXT")
    private String rationale;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getEventId() {
        return this.queueId;
    }

    @Builder
    public AiRecommendation(Long queueId, Integer inputWindowSec, String metricsSnapshotJson,
                            String recommendedPolicyJson, String rationale, Long createdByUserId) {
        this.queueId = queueId;
        this.inputWindowSec = inputWindowSec;
        this.metricsSnapshotJson = metricsSnapshotJson;
        this.recommendedPolicyJson = recommendedPolicyJson;
        this.rationale = rationale;
        this.createdByUserId = createdByUserId;
        this.applied = false;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsApplied(int policyVersion) {
        this.applied = true;
        this.appliedPolicyVersion = policyVersion;
    }

    public static class AiRecommendationBuilder {
        public AiRecommendationBuilder eventId(Long eventId) {
            this.queueId = eventId;
            return this;
        }
    }
}