package dev.willian.agrocontrol.controller;

import dev.willian.agrocontrol.dto.common.PageResponse;
import dev.willian.agrocontrol.dto.field.FieldRequest;
import dev.willian.agrocontrol.dto.field.FieldResponse;
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

@Tag(name = "Talhoes", description = "Gestao de talhoes (fields)")
@RestController
@RequestMapping("/api/v1/fields")
@RequiredArgsConstructor
public class FieldController {

    private final FieldService fieldService;

    @Operation(summary = "Cria um talhao (valida capacidade de area da fazenda)")
    @PostMapping
    public ResponseEntity<FieldResponse> create(@Valid @RequestBody FieldRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fieldService.create(request));
    }

    @Operation(summary = "Lista talhoes (paginados, com filtro opcional por farmId)")
    @GetMapping
    public PageResponse<FieldResponse> list(
            @RequestParam(required = false) Long farmId,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return fieldService.list(farmId, pageable);
    }

    @Operation(summary = "Busca um talhao por id")
    @GetMapping("/{id}")
    public FieldResponse getById(@PathVariable Long id) {
        return fieldService.getById(id);
    }

    @Operation(summary = "Atualiza um talhao")
    @PutMapping("/{id}")
    public FieldResponse update(@PathVariable Long id, @Valid @RequestBody FieldRequest request) {
        return fieldService.update(id, request);
    }

    @Operation(summary = "Remove um talhao (apenas ADMIN ou MANAGER)")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fieldService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
