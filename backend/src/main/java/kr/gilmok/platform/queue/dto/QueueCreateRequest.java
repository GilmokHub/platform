package kr.gilmok.platform.queue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QueueCreateRequest(
        @NotBlank(message = "대기열 이름은 필수입니다.")
        @Size(max = 100, message = "대기열 이름은 100자 이하이어야 합니다.")
        String name,

        @NotBlank(message = "대기열 코드는 필수입니다.")
        @Size(max = 50, message = "대기열 코드는 50자 이하이어야 합니다.")
        String queueCode
) {
}
