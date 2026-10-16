package kr.gilmok.platform.queue.service;

import kr.gilmok.platform.global.exception.CustomException;
import kr.gilmok.platform.policy.repository.PolicyRepository;
import kr.gilmok.platform.queue.dto.QueueCreateRequest;
import kr.gilmok.platform.queue.dto.QueueDetailResponse;
import kr.gilmok.platform.queue.entity.Queue;
import kr.gilmok.platform.queue.exception.QueueErrorCode;
import kr.gilmok.platform.queue.repository.QueueRepository;
import kr.gilmok.platform.tenant.entity.Tenant;
import kr.gilmok.platform.tenant.repository.TenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminQueueService 단위 테스트 (TDD)")
class AdminQueueServiceTest {

    @Mock
    private QueueRepository queueRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private AdminQueueService adminQueueService;

    @Nested
    @DisplayName("신규 대기열 생성 기능")
    class CreateQueueTest {

        @Test
        @DisplayName("관리자가 신규 대기열을 생성할 때(조건), 대기열 엔티티와 기본 트래픽 정책이 함께 영속화되어야 한다(기대 결과)")
        void createQueue_Success_WithDefaultPolicy() {
            // Given
            Long tenantId = 1L;
            Tenant tenant = Tenant.builder()
                    .companyName("테스트 고객사")
                    .adminEmail("test@gilmok.kr")
                    .apiKeyHash("test-api-key-hash")
                    .build();

            QueueCreateRequest request = new QueueCreateRequest("싸이 흠뻑쇼 대기열", "psy-show-2026");

            Queue savedQueue = Queue.builder()
                    .tenant(tenant)
                    .name(request.name())
                    .queueCode(request.queueCode())
                    .status("ACTIVE")
                    .build();

            given(tenantRepository.findById(tenantId)).willReturn(Optional.of(tenant));
            given(queueRepository.existsByQueueCode(request.queueCode())).willReturn(false);
            given(queueRepository.save(any(Queue.class))).willReturn(savedQueue);

            // When
            QueueDetailResponse response = adminQueueService.createQueue(tenantId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.queueCode()).isEqualTo("psy-show-2026");
            assertThat(response.name()).isEqualTo("싸이 흠뻑쇼 대기열");
            assertThat(response.status()).isEqualTo("ACTIVE");

            verify(queueRepository).save(any(Queue.class));
            verify(policyRepository).save(any());
        }

        @Test
        @DisplayName("이미 존재하는 queue_code로 대기열 생성을 요청할 때(조건), 비즈니스 예외가 발생하며 저장이 거부되어야 한다(기대 결과)")
        void createQueue_DuplicateCode_ThrowsException() {
            // Given
            Long tenantId = 1L;
            Tenant tenant = Tenant.builder()
                    .companyName("테스트 고객사")
                    .adminEmail("test@gilmok.kr")
                    .apiKeyHash("test-api-key-hash")
                    .build();

            QueueCreateRequest request = new QueueCreateRequest("중복 대기열", "duplicated-code");

            given(tenantRepository.findById(tenantId)).willReturn(Optional.of(tenant));
            given(queueRepository.existsByQueueCode("duplicated-code")).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> adminQueueService.createQueue(tenantId, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", QueueErrorCode.QUEUE_CODE_DUPLICATED);
        }

        @Test
        @DisplayName("존재하지 않는 tenantId로 대기열 생성을 요청할 때(조건), 테넌트 미존재 예외가 발생해야 한다(기대 결과)")
        void createQueue_TenantNotFound_ThrowsException() {
            // Given
            Long nonExistentTenantId = 999L;
            QueueCreateRequest request = new QueueCreateRequest("대기열", "some-code");

            given(tenantRepository.findById(nonExistentTenantId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> adminQueueService.createQueue(nonExistentTenantId, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", QueueErrorCode.TENANT_NOT_FOUND);
        }
    }
}
