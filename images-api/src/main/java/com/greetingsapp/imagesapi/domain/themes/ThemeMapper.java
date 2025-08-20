package com.greetingsapp.imagesapi.domain.themes;

import com.greetingsapp.imagesapi.dto.themes.ThemeResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ThemeMapper {

    @Mapping(source = "id", target = "themeId")
    @Mapping(source = "name", target = "themeName")
    ThemeResponseDTO themeToThemeResponseDTO(Theme theme); //metodo con la logica base para que el proximo metodo funcione correctamente

    //segundo metodo basado en logica de conversion del primero
    List<ThemeResponseDTO> themeListToThemeResponseDTOList(List<Theme> themes);
}