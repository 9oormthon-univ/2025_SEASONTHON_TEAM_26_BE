package me.nam.dreamdriversserver.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDto {
    @NotBlank
    @Size(min = 3, max = 255)
    private String loginId; // 사용자 아이디

    @NotBlank
    @Size(min = 8, max = 64)
    private String password; // 평문 입력 (저장은 해시)

    @NotBlank
    @Size(min = 2, max = 100)
    private String name; // 이름

    @NotBlank
    @Email
    private String email; // 이메일
}

