package me.nam.dreamdriversserver.domain.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenResponseDto {
    private String message;
    private String loginId;
    private String accessToken;
    private String refreshToken;
    private String tokenType;   // "Bearer"
    private long   expiresIn;   // access 만료(초)

    public static TokenResponseDto of(String loginId, String at, String rt, long exp) {
        return TokenResponseDto.builder()
                .message("로그인 성공")
                .loginId(loginId)
                .accessToken(at)
                .refreshToken(rt)
                .tokenType("Bearer")
                .expiresIn(exp)
                .build();
    }
}
