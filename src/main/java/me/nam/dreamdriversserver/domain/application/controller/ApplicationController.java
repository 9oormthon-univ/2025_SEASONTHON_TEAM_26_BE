package me.nam.dreamdriversserver.domain.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.nam.dreamdriversserver.domain.application.dto.ApplicationRequestDto;
import me.nam.dreamdriversserver.domain.application.dto.ApplicationResponseDto;
import me.nam.dreamdriversserver.domain.application.service.ApplicationService;
import me.nam.dreamdriversserver.common.security.AuthUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "버스 신청 API")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(
            summary = "버스 신청 생성",
            description = "현재 로그인 사용자(principal=userId) 기준으로 버스 신청을 생성합니다. 성공 시 201 Created 반환."
    )
    @PostMapping
    public ResponseEntity<ApplicationResponseDto> create(@Valid @RequestBody ApplicationRequestDto req) {
        Long userId = resolveCurrentUserId();
        ApplicationResponseDto res = applicationService.create(req, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * SecurityContext에서 userId를 추출한다.
     * JwtAuthenticationFilter가 principal로 Long 또는 AuthUser를 세팅한다는 가정.
     */
    private Long resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = (auth != null ? auth.getPrincipal() : null);

        if (principal == null) {
            throw new IllegalStateException("인증 정보(principal)가 없습니다.");
        }
        if (principal instanceof Long l) {
            return l;
        }
        if (principal instanceof AuthUser au) {
            // AuthUser가 record 라면 userId() 존재, class라면 getUserId()를 사용할 수도 있음
            try {
                return au.getId();
            } catch (NoSuchMethodError e) {
                try {
                    return (Long) au.getClass().getMethod("getUserId").invoke(au);
                } catch (Exception ex) {
                    throw new IllegalStateException("AuthUser에서 userId를 읽을 수 없습니다.", ex);
                }
            }
        }
        if (principal instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("principal이 Long 형식이 아닙니다: " + s);
            }
        }
        throw new IllegalStateException("지원하지 않는 principal 타입: " + principal.getClass().getName());
    }
}