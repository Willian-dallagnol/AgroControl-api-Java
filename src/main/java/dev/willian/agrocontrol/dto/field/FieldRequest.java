package dev.willian.agrocontrol.dto.field;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FieldRequest(

        @Schema(example = "Talhao 01 - Leste")
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 120)
        String name,

        @Schema(example = "45.00")
        @NotNull(message = "area e obrigatoria")
        @DecimalMin(value = "0.01", message = "area deve ser maior que zero")
        BigDecimal areaHa,

        @Schema(example = "Latossolo Vermelho")
        @Size(max = 60)
        String soilType,

        @Schema(example = "1")
        @NotNull(message = "farmId e obrigatorio")
        Long farmId
) {
}
