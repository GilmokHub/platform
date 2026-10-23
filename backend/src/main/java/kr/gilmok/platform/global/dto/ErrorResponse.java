package kr.gilmok.platform.global.dto;

import kr.gilmok.platform.global.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final String status;   // "error"
    private final String code;     // "C001"
    private final String message;  // "잘못된 입력입니다."
    private final LocalDateTime timestamp;

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status("error")
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
