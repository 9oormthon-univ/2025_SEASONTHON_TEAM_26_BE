package me.nam.dreamdriversserver.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessExp;
    private final long refreshExp;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-exp-seconds}") long accessExp,
            @Value("${jwt.refresh-exp-seconds}") long refreshExp
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExp = accessExp;
        this.refreshExp = refreshExp;
    }

    public String createAccessToken(String loginId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(loginId)
                .addClaims(Map.of("typ", "access"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessExp)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String loginId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(loginId)
                .addClaims(Map.of("typ", "refresh"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshExp)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getLoginId(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public long getAccessExpSeconds() { return accessExp; }
}