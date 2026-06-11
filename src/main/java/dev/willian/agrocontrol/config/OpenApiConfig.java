package dev.willian.agrocontrol.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura a documentacao OpenAPI/Swagger e o esquema de autenticacao Bearer JWT,
 * de modo que o botao "Authorize" do Swagger UI aceite o token.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI agrocontrolOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AgroControl API")
                        .description("API REST de gestao agricola (Fazendas, Talhoes e Culturas).")
                        .version("1.0.0")
                        .contact(new Contact().name("Willian Dall Agnol")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
