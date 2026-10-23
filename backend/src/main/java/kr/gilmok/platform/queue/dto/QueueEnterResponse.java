package kr.gilmok.platform.queue.dto;

import kr.gilmok.platform.queue.QueueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QueueEnterResponse {
    private final boolean directPass;
    private final QueueStatus status;
    private final String queueKey;
    private final long rank;
    private final long expectedWaitSeconds;
    private final String token;

    public static QueueEnterResponse direct(String token) {
        return QueueEnterResponse.builder()
                .directPass(true)
                .status(QueueStatus.ADMITTABLE)
                .queueKey("DIRECT_PASS")
                .rank(0)
                .expectedWaitSeconds(0)
                .token(token)
                .build();
    }

    public static QueueEnterResponse waiting(String queueKey, long rank, long expectedWaitSeconds) {
        return QueueEnterResponse.builder()
                .directPass(false)
                .status(QueueStatus.WAITING)
                .queueKey(queueKey)
                .rank(rank)
                .expectedWaitSeconds(expectedWaitSeconds)
                .token(null)
                .build();
    }
}