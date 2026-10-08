package kr.gilmok.platform.token.service;

import io.jsonwebtoken.Claims;
import kr.gilmok.platform.token.dto.TokenPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtProvider RS256 비대칭키 단위 테스트")
class JwtProviderTest {

    private JwtProvider jwtProvider;
    private String privateKeyPem;
    private String publicKeyPem;
    private final String keyId = "gilmok-test-key";
    private final String issuer = "https://test.gilmok.kr";

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        privateKeyPem = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        publicKeyPem = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        jwtProvider = new JwtProvider(privateKeyPem, publicKeyPem, keyId, issuer);
    }

    @Test
    @DisplayName("정상 TokenPayload로 RS256 비대칭 서명 토큰 생성 및 공개키 검증 성공")
    void createAndValidateToken_success() {
        // given
        long now = System.currentTimeMillis() / 1000;
        String jti = UUID.randomUUID().toString();
        TokenPayload payload = TokenPayload.builder()
                .jti(jti)
                .id(100L)
                .sub("testuser")
                .status("ADMITTED")
                .role("USER")
                .evt("event-1")
                .res("res-1")
                .rnk(1L)
                .nbf(now)
                .exp(now + 300)
                .build();

        // when
        String token = jwtProvider.createToken(payload);

        // then
        assertThat(token).isNotBlank();
        assertThat(jwtProvider.validateToken(token)).isTrue();

        Claims claims = jwtProvider.getClaims(token);
        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.get("status", String.class)).isEqualTo("ADMITTED");
        assertThat(claims.get("evt", String.class)).isEqualTo("event-1");
        assertThat(jwtProvider.getJti(token)).isEqualTo(jti);
        assertThat(jwtProvider.getRemainingTtlSeconds(token)).isGreaterThan(0L);
    }

    @Test
    @DisplayName("다른 RSA 키로 서명된 토큰이나 변조된 토큰은 검증 실패(false) 반환")
    void validateToken_tamperedToken_returnsFalse() throws Exception {
        // given: 다른 키로 생성된 JwtProvider
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair otherKeyPair = generator.generateKeyPair();
        String otherPrivPem = Base64.getEncoder().encodeToString(otherKeyPair.getPrivate().getEncoded());
        String otherPubPem = Base64.getEncoder().encodeToString(otherKeyPair.getPublic().getEncoded());
        JwtProvider otherProvider = new JwtProvider(otherPrivPem, otherPubPem, "other-key", issuer);

        long now = System.currentTimeMillis() / 1000;
        TokenPayload payload = TokenPayload.builder()
                .jti(UUID.randomUUID().toString())
                .id(100L)
                .sub("hacker")
                .status("ADMITTED")
                .role("USER")
                .evt("event-1")
                .res("res-1")
                .rnk(1L)
                .nbf(now)
                .exp(now + 300)
                .build();
        String forgedToken = otherProvider.createToken(payload);

        // when & then: 원래 jwtProvider의 공개키로는 검증 실패해야 함
        assertThat(jwtProvider.validateToken(forgedToken)).isFalse();
    }

    @Test
    @DisplayName("개인키가 설정되지 않은 상태에서 토큰 생성 시 예외 발생")
    void createToken_withoutPrivateKey_throwsException() {
        JwtProvider verifyOnlyProvider = new JwtProvider("", publicKeyPem, keyId, issuer);

        long now = System.currentTimeMillis() / 1000;
        TokenPayload payload = TokenPayload.builder()
                .sub("testuser")
                .nbf(now)
                .exp(now + 300)
                .build();

        assertThatThrownBy(() -> verifyOnlyProvider.createToken(payload))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RSA Private Key가 설정되지 않았습니다");
    }
}
