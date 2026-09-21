package kr.gilmok.platform.policy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.gilmok.platform.global.dto.ApiResponse;
import kr.gilmok.platform.policy.dto.PolicyHistoryResponse;
import kr.gilmok.platform.policy.dto.PolicyResponse;
import kr.gilmok.platform.policy.dto.PolicyUpdateRequest;
import kr.gilmok.platform.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/events/{eventId}")
@RequiredArgsConstructor
@Tag(name = "Admin Policy", description = "관리자 이벤트 정책 관리 API")
public class AdminPolicyController {

    private final PolicyService policyService;

    @GetMapping("/policy")
    @Operation(summary = "정책 조회", description = "eventId에 해당하는 이벤트 정책을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트 또는 정책 없음")
    })
    public ApiResponse<PolicyResponse> getPolicy(@PathVariable Long eventId) {
        PolicyResponse response = policyService.getPolicyByEventId(eventId);
        return ApiResponse.success(response);
    }

    @GetMapping("/policy/histories")
    @Operation(summary = "정책 히스토리 조회", description = "eventId에 해당하는 이벤트 정책 히스토리를 최신순으로 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트 없음")
    })
    public ApiResponse<Page<PolicyHistoryResponse>> getPolicyHistories(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<PolicyHistoryResponse> histories = policyService.getPolicyHistories(eventId, PageRequest.of(page, size));
        return ApiResponse.success(histories);
    }

    @PutMapping("/policy")
    @Operation(summary = "정책 수정", description = "eventId에 해당하는 이벤트 정책을 수정하고 수정된 정책을 반환합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트 또는 정책 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "동시 수정 충돌")
    })
    public ApiResponse<PolicyResponse> updatePolicy(
            @PathVariable Long eventId,
            @RequestHeader(value = "X-Admin-User-Id", required = false) String adminUserId,
            @Valid @RequestBody PolicyUpdateRequest request) {

        Long updatedByUserId = parseAdminId(adminUserId);
        String updatedByUsername = (adminUserId != null && !adminUserId.isBlank()) ? adminUserId : "admin";
        PolicyResponse response = policyService.updatePolicy(eventId, request, updatedByUserId, updatedByUsername);
        return ApiResponse.success(response);
    }

    @PostMapping("/policy/rollback/{historyId}")
    @Operation(summary = "정책 롤백", description = "historyId에 해당하는 정책 히스토리 스냅샷으로 현재 정책을 되돌립니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "롤백 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트/정책/히스토리 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "동시 수정 충돌")
    })
    public ApiResponse<PolicyResponse> rollbackPolicy(
            @PathVariable Long eventId,
            @PathVariable Long historyId,
            @RequestHeader(value = "X-Admin-User-Id", required = false) String adminUserId
    ) {
        Long rollbackByUserId = parseAdminId(adminUserId);
        String rollbackByUsername = (adminUserId != null && !adminUserId.isBlank()) ? adminUserId : "admin";
        PolicyResponse response = policyService.rollbackPolicy(eventId, historyId, rollbackByUserId, rollbackByUsername);
        return ApiResponse.success(response);
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
