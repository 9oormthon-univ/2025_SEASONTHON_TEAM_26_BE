package me.nam.dreamdriversserver.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDto(@NotBlank String refreshToken) {
}
