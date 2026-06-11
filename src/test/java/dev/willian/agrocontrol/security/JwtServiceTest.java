package dev.willian.agrocontrol.security;

import dev.willian.agrocontrol.config.JwtProperties;
import dev.willian.agrocontrol.domain.Role;
import dev.willian.agrocontrol.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitarios do JwtService: nao precisam de contexto Spring, apenas de um
 * JwtProperties construido na mao (e um record).
 */
class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long-000";

    private final JwtService jwtService =
            new JwtService(new JwtProperties(SECRET, 3_600_000L));

    private final User user = User.builder()
            .id(1L).name("Willian").email("willian@fazenda.dev").role(Role.MANAGER).active(true).build();

    @Test
    void generatedToken_isValidForTheSameUser() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractEmail(token)).isEqualTo("willian@fazenda.dev");
        assertThat(jwtService.isTokenValid(token, "willian@fazenda.dev")).isTrue();
    }

    @Test
    void token_isInvalidForAnotherUser() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, "outro@fazenda.dev")).isFalse();
    }

    @Test
    void malformedToken_isInvalid_andDoesNotThrow() {
        assertThat(jwtService.isTokenValid("nao.e.um.token", "willian@fazenda.dev")).isFalse();
    }

    @Test
    void expiredToken_isInvalid() {
        // expiracao negativa: o token ja nasce expirado.
        JwtService shortLived = new JwtService(new JwtProperties(SECRET, -1_000L));
        String token = shortLived.generateToken(user);

        assertThat(shortLived.isTokenValid(token, "willian@fazenda.dev")).isFalse();
    }
}
