package dev.willian.agrocontrol.dto.crop;

import dev.willian.agrocontrol.domain.CropStatus;
import dev.willian.agrocontrol.validation.ValidHarvestWindow;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@ValidHarvestWindow
public record CropRequest(

        @Schema(example = "Soja Intacta")
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 120)
        String name,

        @Schema(example = "Grao")
        @NotBlank(message = "tipo e obrigatorio")
        @Size(max = 60)
        String type,

        @Schema(example = "PLANTED")
        @NotNull(message = "status e obrigatorio")
        CropStatus status,

        @Schema(example = "2025-10-01")
        LocalDate plantingDate,

        @Schema(example = "2026-02-15")
        LocalDate harvestDate,

        @Schema(example = "1")
        @NotNull(message = "fieldId e obrigatorio")
        Long fieldId
) {
}
