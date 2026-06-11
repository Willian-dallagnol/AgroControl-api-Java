package dev.willian.agrocontrol.service;

import dev.willian.agrocontrol.domain.Farm;
import dev.willian.agrocontrol.domain.User;
import dev.willian.agrocontrol.dto.common.PageResponse;
import dev.willian.agrocontrol.dto.farm.FarmRequest;
import dev.willian.agrocontrol.dto.farm.FarmResponse;
import dev.willian.agrocontrol.exception.ResourceNotFoundException;
import dev.willian.agrocontrol.mapper.FarmMapper;
import dev.willian.agrocontrol.repository.FarmRepository;
import dev.willian.agrocontrol.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FarmService {

    private static final String RESOURCE = "Fazenda";

    private final FarmRepository farmRepository;
    private final FarmMapper farmMapper;
    private final AuthenticatedUserProvider currentUser;

    public FarmResponse create(FarmRequest request) {
        User owner = currentUser.getCurrentUser();
        Farm farm = farmMapper.toEntity(request);
        farm.setUser(owner);
        Farm saved = farmRepository.save(farm);
        log.info("Fazenda criada. id={}, userId={}", saved.getId(), owner.getId());
        return farmMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public FarmResponse getById(Long id) {
        return farmMapper.toResponse(findOwnedFarm(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<FarmResponse> list(String name, Pageable pageable) {
        Long userId = currentUser.getCurrentUserId();
        Page<Farm> page = StringUtils.hasText(name)
                ? farmRepository.findByUser_IdAndNameContainingIgnoreCase(userId, name, pageable)
                : farmRepository.findByUser_Id(userId, pageable);
        return PageResponse.from(page.map(farmMapper::toResponse));
    }

    public FarmResponse update(Long id, FarmRequest request) {
        Farm farm = findOwnedFarm(id);
        farm.setName(request.name());
        farm.setTotalAreaHa(request.totalAreaHa());
        farm.setCity(request.city());
        farm.setState(request.state());
        Farm updated = farmRepository.save(farm);
        log.info("Fazenda atualizada. id={}, userId={}", updated.getId(), currentUser.getCurrentUserId());
        return farmMapper.toResponse(updated);
    }

    public void delete(Long id) {
        farmRepository.delete(findOwnedFarm(id));
        log.info("Fazenda removida. id={}, userId={}", id, currentUser.getCurrentUserId());
    }

    private Farm findOwnedFarm(Long id) {
        return farmRepository.findByIdAndUser_Id(id, currentUser.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE, id));
    }
}
