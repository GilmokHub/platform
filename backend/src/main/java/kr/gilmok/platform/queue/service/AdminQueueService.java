package kr.gilmok.platform.queue.service;

import kr.gilmok.platform.global.exception.CustomException;
import kr.gilmok.platform.policy.entity.Policy;
import kr.gilmok.platform.policy.repository.PolicyRepository;
import kr.gilmok.platform.queue.dto.QueueCreateRequest;
import kr.gilmok.platform.queue.dto.QueueDetailResponse;
import kr.gilmok.platform.queue.entity.Queue;
import kr.gilmok.platform.queue.exception.QueueErrorCode;
import kr.gilmok.platform.queue.repository.QueueRepository;
import kr.gilmok.platform.tenant.entity.Tenant;
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
    private final PolicyRepository policyRepository;

    @Transactional
    public QueueDetailResponse createQueue(Long tenantId, QueueCreateRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new CustomException(QueueErrorCode.TENANT_NOT_FOUND));

        if (queueRepository.existsByQueueCode(request.queueCode())) {
            throw new CustomException(QueueErrorCode.QUEUE_CODE_DUPLICATED);
        }

        Queue queue = Queue.builder()
                .tenant(tenant)
                .name(request.name())
                .queueCode(request.queueCode())
                .status("ACTIVE")
                .build();

        Queue savedQueue = queueRepository.save(queue);
        Policy defaultPolicy = new Policy(savedQueue.getId());
        policyRepository.save(defaultPolicy);

        return QueueDetailResponse.from(savedQueue);
    }

    public List<QueueDetailResponse> getQueuesByTenant(Long tenantId) {
        List<Queue> queues = (tenantId != null)
                ? queueRepository.findAllByTenantId(tenantId)
                : queueRepository.findAll();
        return queues.stream()
                .map(QueueDetailResponse::from)
                .toList();
    }
}
