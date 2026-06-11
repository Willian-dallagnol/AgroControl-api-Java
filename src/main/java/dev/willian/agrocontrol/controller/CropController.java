package dev.willian.agrocontrol.controller;

import dev.willian.agrocontrol.dto.common.PageResponse;
import dev.willian.agrocontrol.dto.crop.CropRequest;
import dev.willian.agrocontrol.dto.crop.CropResponse;
import dev.willian.agrocontrol.service.CropService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Culturas", description = "Gestao de culturas (crops)")
@RestController
@RequestMapping("/api/v1/crops")
@RequiredArgsConstructor
public class CropController {

    private final CropService cropService;

    @Operation(summary = "Cria uma cultura")
    @PostMapping
    public ResponseEntity<CropResponse> create(@Valid @RequestBody CropRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cropService.create(request));
    }

    @Operation(summary = "Lista culturas (paginadas, com filtro opcional por fieldId)")
    @GetMapping
    public PageResponse<CropResponse> list(
            @RequestParam(required = false) Long fieldId,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return cropService.list(fieldId, pageable);
    }

    @Operation(summary = "Busca uma cultura por id")
    @GetMapping("/{id}")
    public CropResponse getById(@PathVariable Long id) {
        return cropService.getById(id);
    }

    @Operation(summary = "Atualiza uma cultura")
    @PutMapping("/{id}")
    public CropResponse update(@PathVariable Long id, @Valid @RequestBody CropRequest request) {
        return cropService.update(id, request);
    }

    @Operation(summary = "Remove uma cultura (apenas ADMIN ou MANAGER)")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cropService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
