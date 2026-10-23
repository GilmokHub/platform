package kr.gilmok.platform.policy.repository;

import kr.gilmok.platform.policy.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByQueueId(Long queueId);

    boolean existsByQueueId(Long queueId);

    default Optional<Policy> findByEventId(Long eventId) {
        return findByQueueId(eventId);
    }

    default boolean existsByEventId(Long eventId) {
        return existsByQueueId(eventId);
    }
}
