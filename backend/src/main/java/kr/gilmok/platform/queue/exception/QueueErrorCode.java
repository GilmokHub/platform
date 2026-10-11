package kr.gilmok.platform.queue.exception;

import kr.gilmok.platform.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QueueErrorCode implements ErrorCode {
    INVALID_QUEUE_KEY(HttpStatus.BAD_REQUEST, "Q003", "유효하지 않거나 만료된 대기열 키입니다."),
    NOT_ADMITTED(HttpStatus.FORBIDDEN, "Q005", "대기열을 통과하지 못했습니다."),
    REDIS_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Q006", "서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해 주세요."),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "Q007", "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."),
    QUEUE_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Q008", "대기열 서비스가 일시적으로 불가합니다. 잠시 후 재시도해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
