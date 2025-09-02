package me.nam.dreamdriversserver.common.config;

import me.nam.dreamdriversserver.common.jwt.JwtAuthenticationFilter;
import me.nam.dreamdriversserver.common.jwt.JwtTokenProvider;
import me.nam.dreamdriversserver.domain.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtTokenProvider tokenProvider,
            UserRepository userRepository
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger & 공개 경로 허용
                        .requestMatchers(
                                "/auth/**",
                                "/v3/api-docs/**", "/swagger-ui/**", "/docs/**"
                        ).permitAll()
                        // 보호가 필요한 API는 인증 요구
                        .requestMatchers("/applications/**", "/buses/**", "/timetables/**").authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults());

        http.addFilterBefore(new JwtAuthenticationFilter(tokenProvider, userRepository),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}