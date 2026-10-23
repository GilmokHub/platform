package kr.gilmok.platform.token.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwksController 단위 테스트")
class JwksControllerTest {

    private JwksController jwksController;
    private String publicKeyPem;
    private final String keyId = "gilmok-key-2026a";

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        publicKeyPem = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        jwksController = new JwksController(publicKeyPem, keyId);
    }

    @Test
    @DisplayName("JWKS 요청 시 RFC 7517 표준 규격에 맞는 RSA 공개키 Set 반환")
    void getJwks_success() {
        // when
        Map<String, Object> jwks = jwksController.getJwks();

        // then
        assertThat(jwks).containsKey("keys");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");

        assertThat(keys).hasSize(1);
        Map<String, Object> jwk = keys.get(0);
        assertThat(jwk.get("kty")).isEqualTo("RSA");
        assertThat(jwk.get("use")).isEqualTo("sig");
        assertThat(jwk.get("alg")).isEqualTo("RS256");
        assertThat(jwk.get("kid")).isEqualTo(keyId);
        assertThat(jwk.get("n")).isNotNull();
        assertThat(jwk.get("e")).isNotNull();
    }

    @Test
    @DisplayName("공개키가 비어있을 경우 빈 keys 리스트 반환")
    void getJwks_emptyKey_returnsEmptyList() {
        JwksController emptyController = new JwksController("", keyId);
        Map<String, Object> jwks = emptyController.getJwks();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
        assertThat(keys).isEmpty();
    }
}
