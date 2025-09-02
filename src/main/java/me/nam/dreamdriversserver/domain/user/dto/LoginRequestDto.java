package me.nam.dreamdriversserver.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequestDto {

    @NotBlank
    private String loginId; // 사용자 아이디

    @NotBlank
    private String password; // 비밀번호
}
