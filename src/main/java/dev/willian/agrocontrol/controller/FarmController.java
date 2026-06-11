package dev.willian.agrocontrol.controller;

import dev.willian.agrocontrol.dto.common.PageResponse;
import dev.willian.agrocontrol.dto.farm.FarmRequest;
import dev.willian.agrocontrol.dto.farm.FarmResponse;
import dev.willian.agrocontrol.dto.field.FieldResponse;
import dev.willian.agrocontrol.service.FarmService;
import dev.willian.agrocontrol.service.FieldService;
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

@Tag(name = "Fazendas", description = "Gestao de fazendas do usuario autenticado")
@RestController
@RequestMapping("/api/v1/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;
    private final FieldService fieldService;

    @Operation(summary = "Cria uma fazenda")
    @PostMapping
    public ResponseEntity<FarmResponse> create(@Valid @RequestBody FarmRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmService.create(request));
    }

    @Operation(summary = "Lista fazendas (paginadas, com filtro opcional por nome)")
    @GetMapping
    public PageResponse<FarmResponse> list(
            @RequestParam(required = false) String name,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return farmService.list(name, pageable);
    }

    @Operation(summary = "Busca uma fazenda por id")
    @GetMapping("/{id}")
    public FarmResponse getById(@PathVariable Long id) {
        return farmService.getById(id);
    }

    @Operation(summary = "Lista os talhoes de uma fazenda (endpoint de relacionamento)")
    @GetMapping("/{id}/fields")
    public PageResponse<FieldResponse> listFields(
            @PathVariable Long id,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return fieldService.list(id, pageable);
    }

    @Operation(summary = "Atualiza uma fazenda")
    @PutMapping("/{id}")
    public FarmResponse update(@PathVariable Long id, @Valid @RequestBody FarmRequest request) {
        return farmService.update(id, request);
    }

    @Operation(summary = "Remove uma fazenda (apenas ADMIN ou MANAGER)")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        farmService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
