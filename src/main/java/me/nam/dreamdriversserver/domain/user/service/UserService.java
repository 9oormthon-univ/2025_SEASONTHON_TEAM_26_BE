package me.nam.dreamdriversserver.domain.user.service;

import lombok.RequiredArgsConstructor;
import me.nam.dreamdriversserver.common.jwt.JwtTokenProvider;
import me.nam.dreamdriversserver.domain.user.dto.LoginRequestDto;
import me.nam.dreamdriversserver.domain.user.dto.LoginRequestDto;
import me.nam.dreamdriversserver.domain.user.dto.LoginResponseDto;
import me.nam.dreamdriversserver.domain.user.dto.LoginResponseDto;
import me.nam.dreamdriversserver.domain.user.dto.RegisterRequestDto;
import me.nam.dreamdriversserver.domain.user.dto.RegisterRequestDto;
import me.nam.dreamdriversserver.domain.user.dto.UserResponseDto;
import me.nam.dreamdriversserver.domain.user.entity.Users;
import me.nam.dreamdriversserver.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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
        Users user = new Users();
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
        String accessToken = jwtTokenProvider.createAccessToken(user.getLoginId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getLoginId());

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

    // 리프레시 토큰으로 재발급
    public LoginResponseDto refresh(String refreshToken) {
        if (!jwtTokenProvider.validate(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 리프레시 토큰입니다.");
        }

        // 현재 JwtTokenProvider는 subject로 loginId를 담고 있으므로 loginId를 추출합니다.
        String loginId = jwtTokenProvider.getLoginId(refreshToken);
        Users user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다."));

        String newAccess = jwtTokenProvider.createAccessToken(user.getLoginId());
        String newRefresh = jwtTokenProvider.createRefreshToken(user.getLoginId());
        long expiresIn = jwtTokenProvider.getAccessExpSeconds();

        return LoginResponseDto.builder()
                .message("토큰 재발급 성공")
                .loginId(user.getLoginId())
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}