package dev.willian.agrocontrol.dto.crop;

import dev.willian.agrocontrol.domain.CropStatus;

import java.time.Instant;
import java.time.LocalDate;

public record CropResponse(
        Long id,
        String name,
        String type,
        CropStatus status,
        LocalDate plantingDate,
        LocalDate harvestDate,
        Long fieldId,
        Instant createdAt,
        Instant updatedAt
) {
}
