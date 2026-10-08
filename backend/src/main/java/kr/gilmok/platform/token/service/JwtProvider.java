package kr.gilmok.platform.token.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import kr.gilmok.platform.token.dto.TokenPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final String keyId;
    private final String issuer;

    public JwtProvider(
            @Value("${app.jwt.private-key:}") String privateKeyPem,
            @Value("${app.jwt.public-key:}") String publicKeyPem,
            @Value("${app.jwt.key-id:gilmok-key-2026a}") String keyId,
            @Value("${app.jwt.issuer:https://platform.gilmok.kr}") String issuer) {
        this.keyId = keyId;
        this.issuer = issuer;
        this.privateKey = parsePrivateKeySafe(privateKeyPem);
        this.publicKey = parsePublicKeySafe(publicKeyPem);
    }

    // Payload 데이터를 받아 RS256 비대칭키로 서명된 JWT 문자열 생성
    public String createToken(TokenPayload payload) {
        if (privateKey == null) {
            throw new IllegalStateException("JWT 생성을 위한 RSA Private Key가 설정되지 않았습니다.");
        }

        var builder = Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setHeaderParam("typ", "JWT")
                .setIssuer(issuer)
                .setSubject(payload.sub())
                .claim("id", payload.id())
                .claim("status", payload.status())
                .claim("role", payload.role())
                .claim("evt", payload.evt())
                .claim("res", payload.res())
                .claim("rnk", payload.rnk())
                .setNotBefore(new Date(payload.nbf() * 1000))
                .setExpiration(new Date(payload.exp() * 1000))
                .signWith(privateKey, SignatureAlgorithm.RS256);

        if (payload.jti() != null) {
            builder.setId(payload.jti());
        }

        return builder.compact();
    }

    // 토큰의 RS256 공개키 서명 및 만료 여부 검증
    public boolean validateToken(String token) {
        if (publicKey == null) {
            return false;
        }
        try {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 검증을 통과한 토큰에서 Claims 추출
    public Claims getClaims(String token) {
        if (publicKey == null) {
            throw new IllegalStateException("JWT 검증을 위한 RSA Public Key가 설정되지 않았습니다.");
        }
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getJti(String token) {
        return getClaims(token).getId();
    }

    public long getRemainingTtlSeconds(String token) {
        Date expiration = getClaims(token).getExpiration();
        long remainingMs = expiration.getTime() - System.currentTimeMillis();
        if (remainingMs <= 0) return 0L;
        return Math.max((remainingMs + 999L) / 1000L, 1L);
    }

    private RSAPrivateKey parsePrivateKeySafe(String pem) {
        if (pem == null || pem.isBlank()) {
            return null;
        }
        try {
            String cleanPem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(cleanPem);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception e) {
            log.error("RSA Private Key 파싱 실패: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 RSA Private Key 형식입니다.", e);
        }
    }

    private RSAPublicKey parsePublicKeySafe(String pem) {
        if (pem == null || pem.isBlank()) {
            return null;
        }
        try {
            String cleanPem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(cleanPem);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception e) {
            log.error("RSA Public Key 파싱 실패: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 RSA Public Key 형식입니다.", e);
        }
    }
}