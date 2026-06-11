package dev.willian.agrocontrol.mapper;

import dev.willian.agrocontrol.domain.User;
import dev.willian.agrocontrol.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface UserMapper {

    UserResponse toResponse(User user);
}
