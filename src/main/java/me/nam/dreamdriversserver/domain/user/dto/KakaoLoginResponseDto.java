// KakaoLoginResponseDto.java  (API 명세 형식)
package me.nam.dreamdriversserver.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoLoginResponseDto {
    private String message;       // "카카오 로그인/가입 성공"
    private String accessToken;
    private String refreshToken;
    private String tokenType;     // "Bearer"
    private long expiresIn;       // 초
    private String userId;        // 우리 서비스 로그인 ID(이메일)
    private String name;
}