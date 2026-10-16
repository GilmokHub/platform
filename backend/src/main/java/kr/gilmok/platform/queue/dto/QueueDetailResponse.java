package kr.gilmok.platform.queue.dto;

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
