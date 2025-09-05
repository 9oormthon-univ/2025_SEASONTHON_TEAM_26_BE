package me.nam.dreamdriversserver.common.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser {
    private final Long id;
    private final String email;
    private final String role; // e.g. "ROLE_USER"

    public AuthUser(Long id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public Collection<? extends GrantedAuthority> toAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }
}