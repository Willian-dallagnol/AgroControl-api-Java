package dev.willian.agrocontrol.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(example = "admin@agrocontrol.dev")
        @NotBlank(message = "email e obrigatorio")
        @Email(message = "email invalido")
        String email,

        @Schema(example = "admin123")
        @NotBlank(message = "senha e obrigatoria")
        String password
) {
}
