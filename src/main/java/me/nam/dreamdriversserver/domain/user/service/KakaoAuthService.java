// KakaoAuthService.java
package me.nam.dreamdriversserver.domain.user.service;

import lombok.RequiredArgsConstructor;
import me.nam.dreamdriversserver.common.jwt.JwtTokenProvider;
import me.nam.dreamdriversserver.common.oauth.KakaoOAuthClient;
import me.nam.dreamdriversserver.domain.user.dto.*;
import me.nam.dreamdriversserver.domain.user.entity.Users;
import me.nam.dreamdriversserver.domain.user.entity.RefreshToken;
import me.nam.dreamdriversserver.domain.user.repository.UserRepository;
import me.nam.dreamdriversserver.domain.user.repository.RefreshTokenRepository;
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
        // 1) code → kakao access token
        KakaoTokenResponse token = kakaoOAuthClient.exchangeToken(code);

        // 2) kakao user info
        KakaoUserResponse profile = kakaoOAuthClient.getUser(token.getAccessToken());
        String email = profile.getKakaoAccount() != null ? profile.getKakaoAccount().getEmail() : null;
        String name  = (profile.getKakaoAccount()!=null && profile.getKakaoAccount().getProfile()!=null)
                ? profile.getKakaoAccount().getProfile().getNickname() : "카카오사용자";

        // 3) 우리 서비스 사용자 upsert
        //    loginId/email 모두 이메일 사용 (카카오는 이메일 동의가 꺼져있으면 null일 수 있음 → 그때는 id 기반 대체)
        String loginId = (email != null && !email.isBlank()) ? email : "kakao_" + profile.getId();

        Users user = userRepository.findByLoginId(loginId)
                .orElseGet(() -> {
                    Users u = new Users();
                    u.setLoginId(loginId);
                    u.setEmail(email != null ? email : loginId);
                    u.setName(name);
                    u.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(u);
                });

        // 4) JWT 발급
        String access  = jwtTokenProvider.createAccessToken(user.getLoginId());
        String refresh = jwtTokenProvider.createRefreshToken(user.getLoginId());

        // 5) RefreshToken 저장(Upsert)
        refreshTokenRepository.findByLoginId(user.getLoginId())
                .ifPresentOrElse(
                        rt -> rt.setToken(refresh),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder().loginId(user.getLoginId()).token(refresh).build()
                        )
                );

        return KakaoLoginResponseDto.builder()
                .message("카카오 로그인/가입 성공")
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessExpSeconds())
                .userId(user.getLoginId())
                .name(user.getName())
                .build();
    }
}