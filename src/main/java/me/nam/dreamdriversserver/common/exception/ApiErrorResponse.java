package me.nam.dreamdriversserver.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(name = "ApiErrorResponse", description = "표준 오류 응답 스키마. 모든 오류 응답은 이 형식을 따름")
public class ApiErrorResponse {
    @Schema(description = "타임스탬프 (서버 LocalDateTime)", example = "2025-09-07T10:21:35.123")
    private final LocalDateTime timestamp;
    @Schema(description = "에러 코드(열거형 문자열)", example = "BAD_REQUEST")
    private final String code;     // 예: BAD_REQUEST
    @Schema(description = "사람이 읽을 수 있는 에러 메시지", example = "유효하지 않은 요청입니다")
    private final String message;  // 예: 유효하지 않은 요청입니다
    @Schema(description = "요청 경로", example = "/auth/login")
    private final String path;     // 요청 경로

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "필드 입력 검증 오류 목록(있을 때만 포함)")
    private final List<FieldError> errors; // 필드 검증 오류가 있을 때만

    @Getter
    @Builder
    @Schema(name = "ApiFieldError", description = "입력 필드 단위 검증 오류")
    public static class FieldError {
        @Schema(description = "필드명", example = "email")
        private final String field;   // 예: email
        @Schema(description = "실패 사유", example = "must not be blank")
        private final String reason;  // 예: must not be blank
        @Schema(description = "거부된 값", example = "")
        private final Object value;   // 예: ""
    }
}