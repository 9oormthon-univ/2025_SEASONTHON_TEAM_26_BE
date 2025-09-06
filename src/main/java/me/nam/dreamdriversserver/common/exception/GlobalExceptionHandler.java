package me.nam.dreamdriversserver.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleApp(AppException ex, HttpServletRequest req) {
        var code = ex.getErrorCode();
        var body = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(code.name())
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.status(code.getStatus()).body(body);
    }

    // @Valid 바인딩 오류
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();

        var body = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(ErrorCode.BAD_REQUEST.name())
                .message(ErrorCode.BAD_REQUEST.getDefaultMessage())
                .path(req.getRequestURI())
                .errors(fieldErrors)
                .build();
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getStatus()).body(body);
    }

    // @Validated on @RequestParam, @PathVariable 등
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        var body = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(ErrorCode.BAD_REQUEST.name())
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getStatus()).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethod(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        var body = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(ErrorCode.BAD_REQUEST.name())
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getStatus()).body(body);
    }

    // 맨 마지막 안전망
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleEtc(Exception ex, HttpServletRequest req) {
        var body = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(ErrorCode.INTERNAL_ERROR.name())
                .message(ErrorCode.INTERNAL_ERROR.getDefaultMessage())
                .path(req.getRequestURI())
                .build();
        // 필요하면 로그 출력
        ex.printStackTrace();
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus()).body(body);
    }

    private ApiErrorResponse.FieldError toFieldError(FieldError fe) {
        return ApiErrorResponse.FieldError.builder()
                .field(fe.getField())
                .reason(fe.getDefaultMessage())
                .value(fe.getRejectedValue())
                .build();
    }
}