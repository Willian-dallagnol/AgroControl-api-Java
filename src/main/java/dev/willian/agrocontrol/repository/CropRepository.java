package dev.willian.agrocontrol.repository;

import dev.willian.agrocontrol.domain.Crop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Long> {

    Optional<Crop> findByIdAndField_Farm_User_Id(Long id, Long userId);

    Page<Crop> findByField_Farm_User_Id(Long userId, Pageable pageable);

    Page<Crop> findByField_IdAndField_Farm_User_Id(Long fieldId, Long userId, Pageable pageable);
}
