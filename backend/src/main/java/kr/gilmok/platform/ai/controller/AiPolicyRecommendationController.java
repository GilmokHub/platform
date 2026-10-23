package kr.gilmok.platform.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.gilmok.platform.ai.dto.AiPolicyRecommendationDto;
import kr.gilmok.platform.ai.dto.ServerSpecRequest;
import kr.gilmok.platform.ai.service.AiPolicyRecommendationService;
import kr.gilmok.platform.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!test")
@Tag(name = "Admin AI", description = "관리자 AI 정책 추천 API")
public class AiPolicyRecommendationController {

    private final AiPolicyRecommendationService aiService;

    @PostMapping("/events/{eventId}/recommendation")
    @Operation(summary = "실시간 정책 추천", description = "서버 스펙과 이벤트 데이터를 기반으로 AI 정책 추천값을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "서버 스펙 입력값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트 없음")
    })
    public ResponseEntity<ApiResponse<AiPolicyRecommendationDto>> getLiveAiRecommendation(
            @PathVariable Long eventId,
            @RequestBody(required = false) ServerSpecRequest serverSpec,
            @RequestHeader(value = "X-Admin-User-Id", required = false) String adminUserIdHeader
    ) {
        Long adminUserId = parseAdminId(adminUserIdHeader);
        AiPolicyRecommendationDto response = aiService.getRecommendation(eventId, adminUserId, serverSpec);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private Long parseAdminId(String adminUserId) {
        if (adminUserId == null || adminUserId.isBlank()) {
            return 1L;
        }
        try {
            return Long.parseLong(adminUserId);
        } catch (NumberFormatException e) {
            return 1L;
        }
    }
}