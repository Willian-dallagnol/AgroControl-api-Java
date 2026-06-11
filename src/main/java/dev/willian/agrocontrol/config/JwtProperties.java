package dev.willian.agrocontrol.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuracao do JWT, ligadas ao prefixo
 * 'agrocontrol.security.jwt' no application.yml.
 */
@ConfigurationProperties(prefix = "agrocontrol.security.jwt")
public record JwtProperties(
        String secret,
        long expirationMs
) {
}
