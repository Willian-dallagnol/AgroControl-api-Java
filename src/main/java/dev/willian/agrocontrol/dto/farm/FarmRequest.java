package dev.willian.agrocontrol.dto.farm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FarmRequest(

        @Schema(example = "Fazenda Santa Maria")
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 120)
        String name,

        @Schema(example = "350.50")
        @NotNull(message = "area total e obrigatoria")
        @DecimalMin(value = "0.01", message = "area total deve ser maior que zero")
        BigDecimal totalAreaHa,

        @Schema(example = "Toledo")
        @Size(max = 120)
        String city,

        @Schema(example = "PR")
        @Size(max = 2, message = "estado deve ter 2 caracteres (UF)")
        String state
) {
}
