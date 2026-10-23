package kr.gilmok.platform.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GlobalErrorCode implements ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C002", "인증이 필요합니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C000", "서버 내부 오류입니다."),
    INVALID_USER(HttpStatus.UNAUTHORIZED, "U001", "유효하지 않은 회원 정보입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
