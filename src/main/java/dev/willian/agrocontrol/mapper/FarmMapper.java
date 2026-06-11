package dev.willian.agrocontrol.mapper;

import dev.willian.agrocontrol.domain.Farm;
import dev.willian.agrocontrol.dto.farm.FarmRequest;
import dev.willian.agrocontrol.dto.farm.FarmResponse;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface FarmMapper {

    FarmResponse toResponse(Farm farm);

    Farm toEntity(FarmRequest request);
}
