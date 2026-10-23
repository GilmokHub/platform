package kr.gilmok.platform.queue.repository;

import kr.gilmok.platform.queue.entity.Queue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QueueRepository extends JpaRepository<Queue, Long> {
    Optional<Queue> findByQueueCode(String queueCode);
    boolean existsByQueueCode(String queueCode);
    List<Queue> findAllByTenantId(Long tenantId);
}
