package dev.willian.agrocontrol.mapper;

import dev.willian.agrocontrol.domain.Crop;
import dev.willian.agrocontrol.dto.crop.CropRequest;
import dev.willian.agrocontrol.dto.crop.CropResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface CropMapper {

    @Mapping(target = "fieldId", source = "field.id")
    CropResponse toResponse(Crop crop);

    Crop toEntity(CropRequest request);
}
