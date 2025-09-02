package me.nam.dreamdriversserver.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponseDto {
    private String message;
    private String loginId;
    private String accessToken;   // JWT 붙일 때 사용
    private String refreshToken;  // JWT 붙일 때 사용
    private String tokenType;     // Bearer
    private long expiresIn;       // 만료 시간(초)

    public static LoginResponseDto success(String loginId) {
        return LoginResponseDto.builder()
                .message("로그인 성공")
                .loginId(loginId)
                .tokenType("Bearer")
                .expiresIn(3600) // 기본 1시간
                .build();
    }
}
