package kr.gilmok.platform.token.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class JwksController {

    private final Map<String, Object> cachedJwks;

    public JwksController(
            @Value("${app.jwt.public-key:}") String publicKeyPem,
            @Value("${app.jwt.key-id:gilmok-key-2026a}") String activeKeyId) {
        this.cachedJwks = initJwks(publicKeyPem, activeKeyId);
    }

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getJwks() {
        return cachedJwks;
    }

    private Map<String, Object> initJwks(String publicKeyPem, String keyId) {
        if (publicKeyPem == null || publicKeyPem.isBlank()) {
            log.warn("JWKS 서빙을 위한 RSA Public Key가 설정되지 않았습니다.");
            return Map.of("keys", List.of());
        }

        try {
            RSAPublicKey publicKey = parsePublicKey(publicKeyPem);
            String n = encodePositiveBigInteger(publicKey.getModulus());
            String e = encodePositiveBigInteger(publicKey.getPublicExponent());

            Map<String, Object> jwk = Map.of(
                    "kty", "RSA",
                    "use", "sig",
                    "alg", "RS256",
                    "kid", keyId,
                    "n", n,
                    "e", e
            );
            return Map.of("keys", List.of(jwk));
        } catch (Exception e) {
            log.error("JWKS 생성 실패: {}", e.getMessage(), e);
            throw new IllegalStateException("JWKS 초기화에 실패했습니다.", e);
        }
    }

    private RSAPublicKey parsePublicKey(String pem) throws Exception {
        String cleanPem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(cleanPem);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
    }

    private String encodePositiveBigInteger(BigInteger b) {
        byte[] bytes = b.toByteArray();
        if (bytes.length > 0 && bytes[0] == 0) {
            byte[] tmp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, tmp, 0, tmp.length);
            bytes = tmp;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
