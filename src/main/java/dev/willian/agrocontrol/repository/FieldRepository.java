package dev.willian.agrocontrol.repository;

import dev.willian.agrocontrol.domain.Field;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface FieldRepository extends JpaRepository<Field, Long> {

    Optional<Field> findByIdAndFarm_User_Id(Long id, Long userId);

    Page<Field> findByFarm_User_Id(Long userId, Pageable pageable);

    Page<Field> findByFarm_IdAndFarm_User_Id(Long farmId, Long userId, Pageable pageable);

    /**
     * Soma das areas dos talhoes de uma fazenda, opcionalmente excluindo um talhao
     * (usado no update para nao contar a propria area duas vezes).
     */
    @Query("""
            SELECT COALESCE(SUM(f.areaHa), 0)
            FROM Field f
            WHERE f.farm.id = :farmId
              AND (:excludeId IS NULL OR f.id <> :excludeId)
            """)
    BigDecimal sumAreaByFarmId(@Param("farmId") Long farmId, @Param("excludeId") Long excludeId);
}
