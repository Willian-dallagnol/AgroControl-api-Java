package dev.willian.agrocontrol.dto.field;

import java.math.BigDecimal;
import java.time.Instant;

public record FieldResponse(
        Long id,
        String name,
        BigDecimal areaHa,
        String soilType,
        Long farmId,
        Instant createdAt,
        Instant updatedAt
) {
}
