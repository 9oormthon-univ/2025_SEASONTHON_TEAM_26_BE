package me.nam.dreamdriversserver.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.nam.dreamdriversserver.domain.user.dto.*;
import me.nam.dreamdriversserver.domain.user.service.KakaoAuthService;
import me.nam.dreamdriversserver.domain.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "회원가입/로그인/JWT 토큰 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final KakaoAuthService kakaoAuthService;

    @Operation(summary = "회원가입", description = "사용자의 정보를 받아 회원가입 처리")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        UserResponseDto response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "로그인", description = "아이디/비밀번호 검증 후 액세스/리프레시 토큰 발급")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 검증하고 새로운 Access/Refresh 발급")
    @PostMapping("/token/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@Valid @RequestBody TokenRefreshRequestDto request) {
        return ResponseEntity.ok(userService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/kakao")
    @Operation(summary = "카카오 로그인", description = "카카오 인가 코드로 로그인/가입 처리 후 서비스 토큰 발급")
    public ResponseEntity<KakaoLoginResponseDto> kakaoLogin(@Valid @RequestBody KakaoLoginRequestDto req) {
        KakaoLoginResponseDto res = kakaoAuthService.loginWithCode(req.getCode());
        return ResponseEntity.ok(res);
    }
}