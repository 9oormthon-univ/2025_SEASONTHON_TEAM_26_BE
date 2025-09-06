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

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public KakaoLoginResponseDto loginWithCode(String code) {
        // 1) code -> kakao access token
        KakaoTokenResponse token = kakaoOAuthClient.exchangeToken(code);

        // 2) kakao user info
        KakaoUserResponse profile = kakaoOAuthClient.getUser(token.getAccessToken());

        String email = (profile.getKakaoAccount() != null)
                ? profile.getKakaoAccount().getEmail()
                : null;

        String nickname = (profile.getKakaoAccount() != null && profile.getKakaoAccount().getProfile() != null)
                ? profile.getKakaoAccount().getProfile().getNickname()
                : "카카오사용자";

        // 이메일이 없으면 카카오 고유 id 기반으로 loginId 대체
        String loginId = (email != null && !email.isBlank())
                ? email
                : "kakao_" + profile.getId();

        // 3) 우리 서비스 사용자 upsert (Users는 new 금지 -> builder 사용)
        Users user = userRepository.findByLoginId(loginId)
                .orElseGet(() -> userRepository.save(
                        Users.builder()
                                .loginId(loginId)
                                .password(null)          // 소셜 로그인이라 비번 없음
                                .name(nickname)
                                .email(email)            // 동의 안하면 null 가능
                                .createdAt(LocalDateTime.now())
                                .build()
                ));

        // 4) JWT 발급 (현 프로젝트 정책: loginId를 subject로 사용)
        String accessToken  = jwtTokenProvider.createAccessToken(user.getUserId(), user.getLoginId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId(),user.getLoginId());

        // 5) RefreshToken upsert
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
                .userId(String.valueOf(user.getUserId()))      // DB PK 반환 (long)
                .name(user.getName())
                .build();
    }
}