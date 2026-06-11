package dev.willian.agrocontrol.dto.auth;

import dev.willian.agrocontrol.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @Schema(example = "Joao da Silva")
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 120)
        String name,

        @Schema(example = "joao@fazenda.dev")
        @NotBlank(message = "email e obrigatorio")
        @Email(message = "email invalido")
        @Size(max = 180)
        String email,

        @Schema(example = "senhaForte123")
        @NotBlank(message = "senha e obrigatoria")
        @Size(min = 6, max = 100, message = "senha deve ter entre 6 e 100 caracteres")
        String password,

        @Schema(example = "OPERATOR", description = "Opcional. Default OPERATOR.")
        Role role
) {
}
