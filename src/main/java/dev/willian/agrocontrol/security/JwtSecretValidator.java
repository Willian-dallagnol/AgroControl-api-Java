package dev.willian.agrocontrol.security;

import dev.willian.agrocontrol.config.JwtProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Valida o segredo JWT no startup. A regra so e aplicada quando o profile "prod" esta ativo,
 * para nao atrapalhar o desenvolvimento local e os testes, que podem usar o valor default.
 *
 * <p>Em producao, a aplicacao NAO sobe se o segredo for vazio, fraco (&lt; 32 bytes) ou
 * o default conhecido - evitando que a autenticacao inteira fique comprometida por descuido.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtSecretValidator {

    /** Mesmo valor default presente no application.yml; nunca deve ir para producao. */
    static final String INSECURE_DEFAULT =
            "change-me-in-production-this-secret-must-be-at-least-32-bytes-long";
    static final int MIN_SECRET_BYTES = 32; // 256 bits, minimo para HS256

    private final JwtProperties jwtProperties;
    private final Environment environment;

    @PostConstruct
    void validateOnStartup() {
        boolean prodProfileActive = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (!prodProfileActive) {
            return; // local/teste: segue com o default sem travar
        }

        String secret = jwtProperties.secret();
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException(
                    "JWT_SECRET ausente em producao. Defina um segredo forte via variavel de ambiente.");
        }
        if (INSECURE_DEFAULT.equals(secret)) {
            throw new IllegalStateException(
                    "JWT_SECRET esta usando o valor default em producao. Defina um segredo proprio e secreto.");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET fraco em producao: use no minimo " + MIN_SECRET_BYTES + " bytes (256 bits).");
        }
        log.info("JWT secret validado para o profile de producao.");
    }
}
