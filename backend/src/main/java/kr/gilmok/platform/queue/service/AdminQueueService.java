package kr.gilmok.platform.queue.service;

import kr.gilmok.platform.queue.dto.QueueCreateRequest;
import kr.gilmok.platform.queue.dto.QueueDetailResponse;
import kr.gilmok.platform.queue.repository.QueueRepository;
import kr.gilmok.platform.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminQueueService {

    private final QueueRepository queueRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public QueueDetailResponse createQueue(Long tenantId, QueueCreateRequest request) {
        // TDD Red 단계: 미구현 상태로 예외 발생
        throw new UnsupportedOperationException("TDD Red: Not yet implemented");
    }

    public List<QueueDetailResponse> getQueuesByTenant(Long tenantId) {
        throw new UnsupportedOperationException("TDD Red: Not yet implemented");
    }
}
