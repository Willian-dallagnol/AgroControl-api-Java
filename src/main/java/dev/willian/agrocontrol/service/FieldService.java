package dev.willian.agrocontrol.service;

import dev.willian.agrocontrol.domain.Farm;
import dev.willian.agrocontrol.domain.Field;
import dev.willian.agrocontrol.dto.common.PageResponse;
import dev.willian.agrocontrol.dto.field.FieldRequest;
import dev.willian.agrocontrol.dto.field.FieldResponse;
import dev.willian.agrocontrol.exception.BusinessRuleException;
import dev.willian.agrocontrol.exception.ResourceNotFoundException;
import dev.willian.agrocontrol.mapper.FieldMapper;
import dev.willian.agrocontrol.repository.FarmRepository;
import dev.willian.agrocontrol.repository.FieldRepository;
import dev.willian.agrocontrol.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FieldService {

    private static final String RESOURCE = "Talhao";
    private static final String FARM_RESOURCE = "Fazenda";

    private final FieldRepository fieldRepository;
    private final FarmRepository farmRepository;
    private final FieldMapper fieldMapper;
    private final AuthenticatedUserProvider currentUser;

    public FieldResponse create(FieldRequest request) {
        // Lock pessimista na fazenda: serializa a checagem de capacidade dentro desta transacao.
        Farm farm = findOwnedFarmForUpdate(request.farmId());
        validateAreaCapacity(farm, request.areaHa(), null);

        Field field = fieldMapper.toEntity(request);
        field.setFarm(farm);
        Field saved = fieldRepository.save(field);
        log.info("Talhao criado. id={}, farmId={}, userId={}",
                saved.getId(), farm.getId(), currentUser.getCurrentUserId());
        return fieldMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public FieldResponse getById(Long id) {
        return fieldMapper.toResponse(findOwnedField(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<FieldResponse> list(Long farmId, Pageable pageable) {
        Long userId = currentUser.getCurrentUserId();
        Page<Field> page = (farmId != null)
                ? fieldRepository.findByFarm_IdAndFarm_User_Id(farmId, userId, pageable)
                : fieldRepository.findByFarm_User_Id(userId, pageable);
        return PageResponse.from(page.map(fieldMapper::toResponse));
    }

    public FieldResponse update(Long id, FieldRequest request) {
        Field field = findOwnedField(id);
        Farm targetFarm = findOwnedFarmForUpdate(request.farmId());
        // Exclui o proprio talhao da soma para nao contabiliza-lo duas vezes.
        validateAreaCapacity(targetFarm, request.areaHa(), field.getId());

        field.setName(request.name());
        field.setAreaHa(request.areaHa());
        field.setSoilType(request.soilType());
        field.setFarm(targetFarm);
        Field updated = fieldRepository.save(field);
        log.info("Talhao atualizado. id={}, userId={}", updated.getId(), currentUser.getCurrentUserId());
        return fieldMapper.toResponse(updated);
    }

    public void delete(Long id) {
        Field field = findOwnedField(id);
        fieldRepository.delete(field);
        log.info("Talhao removido. id={}, userId={}", id, currentUser.getCurrentUserId());
    }

    /**
     * Regra de negocio: a soma das areas dos talhoes nao pode exceder a area total da fazenda.
     */
    private void validateAreaCapacity(Farm farm, BigDecimal newArea, Long excludeFieldId) {
        BigDecimal used = fieldRepository.sumAreaByFarmId(farm.getId(), excludeFieldId);
        BigDecimal projected = used.add(newArea);
        if (projected.compareTo(farm.getTotalAreaHa()) > 0) {
            throw new BusinessRuleException(
                    "Capacidade da fazenda excedida: area ja utilizada %s ha + %s ha = %s ha, mas o total e %s ha."
                            .formatted(used, newArea, projected, farm.getTotalAreaHa()));
        }
    }

    private Field findOwnedField(Long id) {
        return fieldRepository.findByIdAndFarm_User_Id(id, currentUser.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE, id));
    }

    private Farm findOwnedFarmForUpdate(Long farmId) {
        return farmRepository.findByIdAndUser_IdForUpdate(farmId, currentUser.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException(FARM_RESOURCE, farmId));
    }
}
