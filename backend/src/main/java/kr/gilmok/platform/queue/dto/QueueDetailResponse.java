package kr.gilmok.platform.queue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.gilmok.platform.queue.entity.Queue;

import java.time.LocalDateTime;

public record QueueDetailResponse(
        Long queueId,
        Long tenantId,
        String name,
        String queueCode,
        String status,
        LocalDateTime createdAt
) {
    @JsonProperty("eventId")
    public Long eventId() {
        return queueId;
    }

    public static QueueDetailResponse from(Queue queue) {
        return new QueueDetailResponse(
                queue.getId(),
                queue.getTenant() != null ? queue.getTenant().getId() : null,
                queue.getName(),
                queue.getQueueCode(),
                queue.getStatus(),
                queue.getCreatedAt()
        );
    }
}
