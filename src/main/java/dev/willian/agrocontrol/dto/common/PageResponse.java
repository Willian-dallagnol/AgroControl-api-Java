package dev.willian.agrocontrol.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Envelope de paginacao proprio, para nao expor a serializacao interna de Page do Spring
 * (que gera warning e acopla a API a detalhes do framework).
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
