package dev.willian.agrocontrol.dto.farm;

import java.math.BigDecimal;
import java.time.Instant;

public record FarmResponse(
        Long id,
        String name,
        BigDecimal totalAreaHa,
        String city,
        String state,
        Instant createdAt,
        Instant updatedAt
) {
}
