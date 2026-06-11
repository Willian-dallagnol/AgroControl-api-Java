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

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "farms")
public class Farm extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "total_area_ha", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAreaHa;

    @Column(length = 120)
    private String city;

    @Column(length = 2)
    private String state;

    /**
     * Dono da fazenda. Base do isolamento multiusuario: toda consulta filtra por este usuario.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
