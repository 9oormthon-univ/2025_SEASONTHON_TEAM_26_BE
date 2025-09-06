package me.nam.dreamdriversserver.common.config;

import jakarta.servlet.http.HttpServletResponse;
import me.nam.dreamdriversserver.common.jwt.JwtAuthenticationFilter;
import me.nam.dreamdriversserver.common.jwt.JwtTokenProvider;
import me.nam.dreamdriversserver.domain.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/actuator/**").permitAll()   // ★ 루트 & 액추에이터 전부 허용
                        .requestMatchers(
                                "/auth/**",
                                "/v3/api-docs/**", "/swagger-ui/**", "/docs/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/regions/search",
                                "/timetables",
                                "/applications/summary",
                                "/buses/live",
                                "/stops/nearest",
                                "/map/overview"
                        ).permitAll()
                        .requestMatchers("/applications/**").authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(h -> h.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"error\":\"UNAUTHORIZED\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"error\":\"FORBIDDEN\"}");
                        })
                );

        http.addFilterBefore(
                new JwtAuthenticationFilter(tokenProvider, userRepository),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of(
                "*"
                // 필요시 특정 도메인만 남기세요: "http://localhost:5173", "https://your-web.app" 등
        ));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Location","Authorization","Link"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}