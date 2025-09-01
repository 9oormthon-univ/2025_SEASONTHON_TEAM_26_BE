package me.nam.dreamdriversserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF는 일단 개발 단계에서는 끔
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/docs/**",        // Swagger UI
                                "/v3/api-docs/**",     // OpenAPI spec
                                "/swagger-ui/**",      // Swagger 리소스
                                "/swagger-resources/**"
                        ).permitAll() // 위 경로는 인증 필요 없음
                        .anyRequest().permitAll() // 나머지는 일단 전부 허용 (추후 조정)
                );

        return http.build();
    }
}