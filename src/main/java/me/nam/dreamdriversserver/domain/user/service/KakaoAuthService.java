// KakaoAuthService.java
package me.nam.dreamdriversserver.domain.user.service;

import lombok.RequiredArgsConstructor;
import me.nam.dreamdriversserver.common.jwt.JwtTokenProvider;
import me.nam.dreamdriversserver.common.oauth.KakaoOAuthClient;
import me.nam.dreamdriversserver.domain.user.dto.KakaoLoginResponseDto;
import me.nam.dreamdriversserver.domain.user.dto.KakaoTokenResponse;
import me.nam.dreamdriversserver.domain.user.dto.KakaoUserResponse;
import me.nam.dreamdriversserver.domain.user.entity.RefreshToken;
import me.nam.dreamdriversserver.domain.user.entity.Users;
import me.nam.dreamdriversserver.domain.user.repository.RefreshTokenRepository;
import me.nam.dreamdriversserver.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// KakaoAuthService.java
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public KakaoLoginResponseDto loginWithAccessToken(String rawAccessToken) {
        // "Bearer xxx" 형태로 와도 처리
        String kakaoAccessToken = stripBearer(rawAccessToken);

        // 1) 카카오 토큰 검증 + 프로필 조회
        KakaoUserResponse profile = kakaoOAuthClient.getUser(kakaoAccessToken);

        // 2) 우리 쪽 식별 키 결정
        String email = profile.getKakaoAccount() != null ? profile.getKakaoAccount().getEmail() : null;
        String nickname = (profile.getKakaoAccount() != null && profile.getKakaoAccount().getProfile() != null)
                ? profile.getKakaoAccount().getProfile().getNickname()
                : "카카오사용자";

        // 이메일이 없으면 kakao id 기반으로 loginId 구성
        String loginId = (email != null && !email.isBlank()) ? email : "kakao_" + profile.getId();

        // 3) upsert
        Users user = userRepository.findByLoginId(loginId)
                .orElseGet(() -> userRepository.save(
                        Users.builder()
                                .loginId(loginId)
                                .password(null)
                                .name(nickname)
                                .email(email)
                                .createdAt(java.time.LocalDateTime.now())
                                .build()
                ));

        // 4) JWT 발급
        String accessToken  = jwtTokenProvider.createAccessToken(user.getUserId(), user.getLoginId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId(), user.getLoginId());

        // 5) RT 저장/갱신
        refreshTokenRepository.findByLoginId(user.getLoginId())
                .ifPresentOrElse(
                        rt -> rt.setToken(refreshToken),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .loginId(user.getLoginId())
                                        .token(refreshToken)
                                        .build()
                        )
                );

        // 6) 응답
        return KakaoLoginResponseDto.builder()
                .message("카카오 로그인/가입 성공")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessExpSeconds())
                .userId(String.valueOf(user.getUserId()))
                .name(user.getName())
                .build();
    }

    private String stripBearer(String token) {
        if (token == null) return "";
        String t = token.trim();
        return (t.regionMatches(true, 0, "Bearer ", 0, 7)) ? t.substring(7).trim() : t;
    }
}