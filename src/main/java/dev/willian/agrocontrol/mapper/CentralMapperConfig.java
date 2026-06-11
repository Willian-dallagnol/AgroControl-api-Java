package dev.willian.agrocontrol.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Configuracao central do MapStruct: gera beans Spring e ignora alvos nao mapeados
 * (ids, auditoria e relacionamentos sao definidos manualmente na camada de servico).
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CentralMapperConfig {
}
