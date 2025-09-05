package me.nam.dreamdriversserver.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    /** jjwt 0.9.1에서는 SecretKey 객체 대신 문자열/byte[]를 바로 넣어도 됩니다. */
    private final String secret;

    @Getter
    private final long accessExpSeconds;

    @Getter
    private final long refreshExpSeconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret, // 그냥 문자열 사용 (Base64 디코딩 X)
            @Value("${jwt.access-exp-seconds:1800}") long accessExpSeconds,
            @Value("${jwt.refresh-exp-seconds:1209600}") long refreshExpSeconds
    ) {
        this.secret = secret;
        this.accessExpSeconds = accessExpSeconds;
        this.refreshExpSeconds = refreshExpSeconds;
    }

    /** 권장 시그니처 */
    public String createAccessToken(Long userId, String loginId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpSeconds * 1000);

        return Jwts.builder()
                .setSubject(loginId)          // sub = loginId
                .claim("uid", userId)         // 사용자 PK
                .claim("typ", "access")
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String createRefreshToken(Long userId, String loginId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpSeconds * 1000);

        return Jwts.builder()
                .setSubject(loginId)          // sub = loginId
                .claim("uid", userId)         // 사용자 PK
                .claim("typ", "refresh")
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /** 구시그니처는 사용 금지 */
    @Deprecated
    public String createAccessToken(String loginId) {
        throw new UnsupportedOperationException("Use createAccessToken(userId, loginId)");
    }

    @Deprecated
    public String createRefreshToken(String loginId) {
        throw new UnsupportedOperationException("Use createRefreshToken(userId, loginId)");
    }

    /** 0.9.1 검증 방식 */
    public boolean validate(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) { // 만료/서명오류 등 모두 false
            return false;
        }
    }

    /** 필터 호환용(예전 메서드명) */
    public boolean validateToken(String token) { return validate(token); }

    public Long getUserId(String token) {
        Claims c = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        Object uid = c.get("uid");
        if (uid instanceof Number n) return n.longValue();
        throw new IllegalStateException("Invalid uid claim");
    }

    public String getLoginId(String token) {
        Claims c = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        return c.getSubject();
    }

    /** 필터 호환용(이메일을 sub로 썼다면 그대로 반환) */
    public String getEmail(String token) { return getLoginId(token); }

    /** 필터 호환용(토큰에 role을 넣지 않았다면 기본값) */
    public String getRole(String token) {
        Claims c = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        Object r = c.get("role");
        return r != null ? String.valueOf(r) : "USER";
    }

    public boolean isRefreshToken(String token) {
        Claims c = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        Object typ = c.get("typ");
        return "refresh".equals(typ);
    }
}