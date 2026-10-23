package kr.gilmok.platform.queue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QueueRegisterRequest {

    @NotBlank(message = "eventId는 필수입니다.")
    private String eventId;
}
