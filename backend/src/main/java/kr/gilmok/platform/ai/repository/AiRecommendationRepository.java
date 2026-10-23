package kr.gilmok.platform.ai.repository;

import kr.gilmok.platform.ai.entity.AiRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, Long> {

    List<AiRecommendation> findByQueueIdOrderByIdDesc(Long queueId);

    default List<AiRecommendation> findByEventIdOrderByIdDesc(Long eventId) {
        return findByQueueIdOrderByIdDesc(eventId);
    }
}
