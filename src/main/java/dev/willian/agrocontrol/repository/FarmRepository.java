package dev.willian.agrocontrol.repository;

import dev.willian.agrocontrol.domain.Farm;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Todas as consultas sao escopadas pelo dono (user.id) para garantir isolamento de dados.
 * O underscore (User_Id) forca o caminho de navegacao farm.user.id.
 */
public interface FarmRepository extends JpaRepository<Farm, Long> {

    Optional<Farm> findByIdAndUser_Id(Long id, Long userId);

    /**
     * Igual ao findByIdAndUser_Id, mas com lock pessimista de escrita (SELECT ... FOR UPDATE).
     * Usado ao criar/atualizar talhoes: serializa a checagem de capacidade de area da fazenda,
     * evitando race condition (duas requisicoes simultaneas estourarem a area total).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Farm f WHERE f.id = :id AND f.user.id = :userId")
    Optional<Farm> findByIdAndUser_IdForUpdate(@Param("id") Long id, @Param("userId") Long userId);

    Page<Farm> findByUser_Id(Long userId, Pageable pageable);

    Page<Farm> findByUser_IdAndNameContainingIgnoreCase(Long userId, String name, Pageable pageable);

    boolean existsByIdAndUser_Id(Long id, Long userId);
}
