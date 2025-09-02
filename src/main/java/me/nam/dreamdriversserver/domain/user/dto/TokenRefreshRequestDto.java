package me.nam.dreamdriversserver.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TokenRefreshRequestDto {
    @NotBlank
    private String refreshToken;
}