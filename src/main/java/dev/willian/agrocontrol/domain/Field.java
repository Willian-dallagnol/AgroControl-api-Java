package dev.willian.agrocontrol.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Talhao. Pertence a uma Fazenda; o dono e herdado via Farm.user.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "fields")
public class Field extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "area_ha", nullable = false, precision = 12, scale = 2)
    private BigDecimal areaHa;

    @Column(name = "soil_type", length = 60)
    private String soilType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;
}
