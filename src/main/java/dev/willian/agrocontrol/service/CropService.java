package dev.willian.agrocontrol.service;

import dev.willian.agrocontrol.domain.Crop;
import dev.willian.agrocontrol.domain.Field;
import dev.willian.agrocontrol.dto.common.PageResponse;
import dev.willian.agrocontrol.dto.crop.CropRequest;
import dev.willian.agrocontrol.dto.crop.CropResponse;
import dev.willian.agrocontrol.exception.ResourceNotFoundException;
import dev.willian.agrocontrol.mapper.CropMapper;
import dev.willian.agrocontrol.repository.CropRepository;
import dev.willian.agrocontrol.repository.FieldRepository;
import dev.willian.agrocontrol.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CropService {

    private static final String RESOURCE = "Cultura";
    private static final String FIELD_RESOURCE = "Talhao";

    private final CropRepository cropRepository;
    private final FieldRepository fieldRepository;
    private final CropMapper cropMapper;
    private final AuthenticatedUserProvider currentUser;

    public CropResponse create(CropRequest request) {
        Field field = findOwnedField(request.fieldId());
        Crop crop = cropMapper.toEntity(request);
        crop.setField(field);
        Crop saved = cropRepository.save(crop);
        log.info("Cultura criada. id={}, fieldId={}, userId={}",
                saved.getId(), field.getId(), currentUser.getCurrentUserId());
        return cropMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CropResponse getById(Long id) {
        return cropMapper.toResponse(findOwnedCrop(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<CropResponse> list(Long fieldId, Pageable pageable) {
        Long userId = currentUser.getCurrentUserId();
        Page<Crop> page = (fieldId != null)
                ? cropRepository.findByField_IdAndField_Farm_User_Id(fieldId, userId, pageable)
                : cropRepository.findByField_Farm_User_Id(userId, pageable);
        return PageResponse.from(page.map(cropMapper::toResponse));
    }

    public CropResponse update(Long id, CropRequest request) {
        Crop crop = findOwnedCrop(id);
        Field targetField = findOwnedField(request.fieldId());

        crop.setName(request.name());
        crop.setType(request.type());
        crop.setStatus(request.status());
        crop.setPlantingDate(request.plantingDate());
        crop.setHarvestDate(request.harvestDate());
        crop.setField(targetField);
        Crop updated = cropRepository.save(crop);
        log.info("Cultura atualizada. id={}, userId={}", updated.getId(), currentUser.getCurrentUserId());
        return cropMapper.toResponse(updated);
    }

    public void delete(Long id) {
        cropRepository.delete(findOwnedCrop(id));
        log.info("Cultura removida. id={}, userId={}", id, currentUser.getCurrentUserId());
    }

    private Crop findOwnedCrop(Long id) {
        return cropRepository.findByIdAndField_Farm_User_Id(id, currentUser.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE, id));
    }

    private Field findOwnedField(Long fieldId) {
        return fieldRepository.findByIdAndFarm_User_Id(fieldId, currentUser.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException(FIELD_RESOURCE, fieldId));
    }
}
