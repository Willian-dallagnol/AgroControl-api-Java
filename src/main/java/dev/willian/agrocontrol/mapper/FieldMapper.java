package dev.willian.agrocontrol.mapper;

import dev.willian.agrocontrol.domain.Field;
import dev.willian.agrocontrol.dto.field.FieldRequest;
import dev.willian.agrocontrol.dto.field.FieldResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface FieldMapper {

    @Mapping(target = "farmId", source = "farm.id")
    FieldResponse toResponse(Field field);

    Field toEntity(FieldRequest request);
}
