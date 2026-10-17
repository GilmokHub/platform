package kr.gilmok.platform.queue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.gilmok.platform.global.dto.ApiResponse;
import kr.gilmok.platform.queue.dto.QueueCreateRequest;
import kr.gilmok.platform.queue.dto.QueueDetailResponse;
import kr.gilmok.platform.queue.service.AdminQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/queues")
@RequiredArgsConstructor
@Tag(name = "Admin Queue", description = "관리자 대기열 리소스 관리 API")
public class AdminQueueController {

    private final AdminQueueService adminQueueService;

    @GetMapping
    @Operation(summary = "대기열 목록 조회", description = "등록된 대기열 목록을 조회합니다.")
    public ApiResponse<List<QueueDetailResponse>> getQueues(
            @RequestParam(required = false) Long tenantId) {
        return ApiResponse.success(adminQueueService.getQueuesByTenant(tenantId));
    }

    @GetMapping("/{queueId}")
    @Operation(summary = "대기열 단건 조회", description = "queueId에 해당하는 대기열 상세 정보를 조회합니다.")
    public ApiResponse<QueueDetailResponse> getQueue(@PathVariable Long queueId) {
        return ApiResponse.success(adminQueueService.getQueue(queueId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "신규 대기열 생성", description = "신규 대기열과 기본 트래픽 정책을 생성합니다.")
    public ApiResponse<QueueDetailResponse> createQueue(
            @RequestParam(defaultValue = "1") Long tenantId,
            @Valid @RequestBody QueueCreateRequest request) {
        return ApiResponse.success(adminQueueService.createQueue(tenantId, request));
    }
}
