package dev.willian.agrocontrol.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Habilita auditoria JPA (@CreatedDate / @LastModifiedDate nas entidades).
 * Mantido fora da classe principal para facilitar testes que nao precisam de auditoria.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
