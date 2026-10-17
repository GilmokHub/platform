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
import java.util.UUID;

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

        String queueCode = (request.queueCode() != null && !request.queueCode().isBlank())
                ? request.queueCode().trim()
                : generateUniqueQueueCode();

        if (queueRepository.existsByQueueCode(queueCode)) {
            throw new CustomException(QueueErrorCode.QUEUE_CODE_DUPLICATED);
        }

        Queue queue = Queue.builder()
                .tenant(tenant)
                .name(request.name())
                .queueCode(queueCode)
                .status("ACTIVE")
                .build();

        Queue savedQueue = queueRepository.save(queue);
        Policy defaultPolicy = new Policy(savedQueue.getId());
        policyRepository.save(defaultPolicy);

        return QueueDetailResponse.from(savedQueue);
    }

    private String generateUniqueQueueCode() {
        for (int i = 0; i < 5; i++) {
            String candidate = "q_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            if (!queueRepository.existsByQueueCode(candidate)) {
                return candidate;
            }
        }
        return "q_" + UUID.randomUUID().toString().replace("-", "");
    }

    public List<QueueDetailResponse> getQueuesByTenant(Long tenantId) {
        List<Queue> queues = (tenantId != null)
                ? queueRepository.findAllByTenantId(tenantId)
                : queueRepository.findAll();
        return queues.stream()
                .map(QueueDetailResponse::from)
                .toList();
    }

    public QueueDetailResponse getQueue(Long queueId) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new CustomException(QueueErrorCode.QUEUE_NOT_FOUND));
        return QueueDetailResponse.from(queue);
    }
}
