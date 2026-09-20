package kr.gilmok.platform.queue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.gilmok.platform.global.dto.ApiResponse;
import kr.gilmok.platform.policy.dto.PolicyCacheDto;
import kr.gilmok.platform.policy.filter.PolicyFilter;
import kr.gilmok.platform.policy.repository.PolicyCacheRepository;
import kr.gilmok.platform.queue.QueueStatus;
import kr.gilmok.platform.queue.dto.*;
import kr.gilmok.platform.queue.service.QueueService;
import kr.gilmok.platform.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/queue")
@RequiredArgsConstructor
@Tag(name = "Queue", description = "대기열 API")
public class QueueController {

    private final QueueService queueService;
    private final TokenService tokenService;
    private final PolicyCacheRepository policyCacheRepository;

    /**
     * SaaS 대기열 진입 엔드포인트
     * - 평상시 (ROUTING_DISABLED): 대기열 Redis 적재 없이 즉시 입장 토큰 발급 (0초 통과)
     * - 트래픽 집중 시 (ROUTING_ENABLED): Redis Sorted Set 대기열 등록 후 순번 반환
     */
    @PostMapping("/enter")
    @Operation(summary = "대기열 진입", description = "고객사 식별키(clientKey)와 eventId로 대기열에 진입하거나 평상시 즉시 통과합니다.")
    public ResponseEntity<ApiResponse<QueueEnterResponse>> enter(
            @Valid @RequestBody QueueEnterRequest request,
            HttpServletRequest httpRequest) {

        Long userId = request.getUserId() != null ? request.getUserId() : 0L;
        String username = "user_" + userId;
        String eventId = request.getEventId();

        // 1. 정책 조회 (PolicyFilter attribute 또는 Redis 직접 조회)
        PolicyCacheDto policy = (PolicyCacheDto) httpRequest.getAttribute(PolicyFilter.POLICY_CACHE_ATTR);
        if (policy == null) {
            try {
                Long eventIdLong = Long.parseLong(eventId);
                policy = policyCacheRepository.find(eventIdLong).orElse(null);
            } catch (NumberFormatException ignored) {
            }
        }

        // 2. 동적 트래픽 제어 분기
        // 평상시: 게이트 모드가 ROUTING_DISABLED 이면 대기열 없이 즉시 통과
        if (policy != null && "ROUTING_DISABLED".equals(policy.gateMode())) {
            log.info("Queue bypass (ROUTING_DISABLED): clientKey={}, eventId={}, userId={}",
                    request.getClientKey(), eventId, userId);
            String directToken = tokenService.issueAdmissionToken(eventId, null, userId, username, 0);
            return ResponseEntity.ok(ApiResponse.success(QueueEnterResponse.direct(directToken)));
        }

        // 트래픽 집중 시: 대기열 Redis 적재
        QueueRegisterRequest regReq = new QueueRegisterRequest(eventId);
        QueueRegisterResponse regRes = queueService.register(userId, regReq, policy);

        QueueEnterResponse enterResponse = QueueEnterResponse.waiting(
                regRes.getQueueKey(),
                regRes.getPosition(),
                regRes.getEtaSeconds()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(enterResponse));
    }

    /**
     * 대기열 상태 및 입장 토큰 조회
     */
    @GetMapping("/status")
    @Operation(summary = "대기열 상태 조회", description = "대기 순번 및 대기열 통과 시 입장 토큰을 조회합니다.")
    public ResponseEntity<ApiResponse<QueueStatusResponse>> getStatus(
            @RequestParam String eventId,
            @RequestParam(required = false) String queueKey,
            @RequestHeader(value = "X-Queue-Key", required = false) String queueKeyHeader,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String clientKey) {

        String effectiveQueueKey = (queueKey != null && !queueKey.isBlank()) ? queueKey : queueKeyHeader;

        // 평상시 직통 통과 토큰인 경우
        if ("DIRECT_PASS".equals(effectiveQueueKey)) {
            String token = tokenService.issueAdmissionToken(eventId, null, userId != null ? userId : 0L, "user", 0);
            return ResponseEntity.ok(ApiResponse.success(
                    new QueueStatusResponse(QueueStatus.ADMITTABLE, 0, 0, 0, 1000, token)
            ));
        }

        Long effectiveUserId = userId != null ? userId : 0L;

        String username = "user_" + effectiveUserId;

        if (effectiveQueueKey == null || effectiveQueueKey.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(
                    new QueueStatusResponse(QueueStatus.EXPIRED, 0, 0, 0, 0, null)
            ));
        }

        QueueStatusResponse response = queueService.getStatus(eventId, effectiveQueueKey, username, effectiveUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
