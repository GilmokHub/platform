package kr.gilmok.platform.policy.repository;

import kr.gilmok.platform.policy.entity.PolicyHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PolicyHistoryRepository extends JpaRepository<PolicyHistory, Long> {

    Page<PolicyHistory> findByQueueIdOrderByCreatedAtDescIdDesc(Long queueId, Pageable pageable);

    Optional<PolicyHistory> findByIdAndQueueId(Long id, Long queueId);

    default Page<PolicyHistory> findByEventIdOrderByCreatedAtDescIdDesc(Long eventId, Pageable pageable) {
        return findByQueueIdOrderByCreatedAtDescIdDesc(eventId, pageable);
    }

    default Optional<PolicyHistory> findByIdAndEventId(Long id, Long eventId) {
        return findByIdAndQueueId(id, eventId);
    }
}