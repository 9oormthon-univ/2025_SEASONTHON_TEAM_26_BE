package me.nam.dreamdriversserver.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ApiErrorResponse {
    private final LocalDateTime timestamp;
    private final String code;     // 예: BAD_REQUEST
    private final String message;  // 예: 유효하지 않은 요청입니다
    private final String path;     // 요청 경로

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<FieldError> errors; // 필드 검증 오류가 있을 때만

    @Getter
    @Builder
    public static class FieldError {
        private final String field;   // 예: email
        private final String reason;  // 예: must not be blank
        private final Object value;   // 예: ""
    }
}