// KakaoLoginRequestDto.java
package me.nam.dreamdriversserver.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class KakaoLoginRequestDto {
    @NotBlank
    private String accessToken;   // code → accessToken 으로 변경

    public String normalizedToken() {
        return accessToken == null ? null : accessToken.strip(); // 앞뒤 공백/개행 제거
    }
}