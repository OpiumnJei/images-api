package com.greetingsapp.imagesapi.domain.users;

import com.greetingsapp.imagesapi.dto.users.UserResponseDTO;
import org.mapstruct.Mapper;

// 1. componentModel="spring" le dice a MapStruct que genere un @Component,
//    haciéndolo un bean que Spring puede descubrir e inyectar.
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO userToUserResponseDTO(User user);
}
