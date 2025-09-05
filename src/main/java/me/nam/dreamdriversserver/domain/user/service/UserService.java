package me.nam.dreamdriversserver.domain.user.service;

import lombok.RequiredArgsConstructor;
import me.nam.dreamdriversserver.common.exception.NotFoundException;
import me.nam.dreamdriversserver.common.jwt.JwtTokenProvider;
import me.nam.dreamdriversserver.domain.user.dto.*;
import me.nam.dreamdriversserver.domain.user.dto.LoginRequestDto;
import me.nam.dreamdriversserver.domain.user.dto.LoginResponseDto;
import me.nam.dreamdriversserver.domain.user.dto.RegisterRequestDto;
import me.nam.dreamdriversserver.domain.user.entity.RefreshToken;
import me.nam.dreamdriversserver.domain.user.entity.Users;
import me.nam.dreamdriversserver.domain.user.repository.RefreshTokenRepository;
import me.nam.dreamdriversserver.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // 회원가입
    public UserResponseDto register(RegisterRequestDto req) {
        // 중복 체크: loginId, email
        if (userRepository.existsByLoginId(req.getLoginId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 이메일입니다.");
        }

        // 저장
        Users user = userRepository.findByLoginId(req.getLoginId())
         .orElseThrow(() -> new NotFoundException("user"));
        user.setLoginId(req.getLoginId());
        user.setPassword(passwordEncoder.encode(req.getPassword())); // 비밀번호 해시
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setCreatedAt(LocalDateTime.now());

        Users saved = userRepository.save(user);

        return UserResponseDto.from(saved);
    }

    // 로그인 (JWT 발급)
    public LoginResponseDto login(LoginRequestDto req) {
        Users user = userRepository.findByLoginId(req.getLoginId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // JwtTokenProvider는 loginId만 받도록 되어 있으므로 loginId 기준으로 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(),user.getLoginId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId(), user.getLoginId());

        long expiresIn = jwtTokenProvider.getAccessExpSeconds();

        return LoginResponseDto.builder()
                .message("로그인 성공")
                .loginId(user.getLoginId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }


    @Transactional
    public TokenResponseDto issueTokens(Users user) {
        Long userId = user.getUserId();      // 엔티티의 PK
        String loginId = user.getLoginId();  // 로그인 식별자(이메일 등)

        String access  = jwtTokenProvider.createAccessToken(userId, loginId);
        String refresh = jwtTokenProvider.createRefreshToken(userId, loginId);

        upsertRefreshToken(loginId, refresh);

        return TokenResponseDto.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessExpSeconds())
                .build();
    }

    /**
     * Refresh 토큰으로 재발급
     */
    @Transactional
    public TokenResponseDto refresh(String refreshToken) {
        // 1) 토큰 유효성 + refresh 타입 여부
        if (!jwtTokenProvider.validate(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰");
        }

        // 2) 토큰에서 사용자 식별값 추출
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String loginId = jwtTokenProvider.getLoginId(refreshToken);

        // 3) 사용자 확인 (필요 시)
        Users user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + loginId));
        if (!user.getUserId().equals(userId)) {
            throw new IllegalStateException("토큰의 uid와 사용자 정보가 일치하지 않습니다.");
        }

        // 4) 새 토큰 발급 (★ 여기서 반드시 userId + loginId 사용)
        String newAccess  = jwtTokenProvider.createAccessToken(userId, loginId);
        String newRefresh = jwtTokenProvider.createRefreshToken(userId, loginId);

        // 5) Refresh 저장(업서트)
        upsertRefreshToken(loginId, newRefresh);

        return TokenResponseDto.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessExpSeconds())
                .build();
    }

    private void upsertRefreshToken(String loginId, String refreshToken) {
        refreshTokenRepository.findByLoginId(loginId)
                .ifPresentOrElse(
                        rt -> rt.setToken(refreshToken),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .loginId(loginId)
                                        .token(refreshToken)
                                        .build()
                        )
                );
    }
}