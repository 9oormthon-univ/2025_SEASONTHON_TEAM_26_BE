package me.nam.dreamdriversserver.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long userId;
    private String loginId;
    private String password;
    private String name;
    private String email;
    private String createdAt;
}

