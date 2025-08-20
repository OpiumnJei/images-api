package com.greetingsapp.imagesapi.domain.images;

import com.greetingsapp.imagesapi.dto.images.ImageResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

// 1. componentModel="spring" le dice a MapStruct que genere un @Component,
//    haciéndolo un bean que Spring puede descubrir e inyectar.
@Mapper(componentModel = "spring")
public interface ImageMapper {

    //instancia de imageMapper, usada cuando no se esta usando la inyeccion de dependencias
    //ImageMapper mapper = Mappers.getMapper(ImageMapper.class);

    // source: nombre del atributo que queremos mapear(atributo origen)
    // target: nombre del campo al que se va a mapear el atributo(atributo destino)
    @Mapping(source = "id", target = "imageId")
    @Mapping(source = "name", target = "imageName")
    @Mapping(source = "description", target = "imageDescription")
    @Mapping(source = "url", target = "imageUrl")
    ImageResponseDTO imageToImageResponseDTO(Image image);

    // Blueprint para un metodo:
    // MapStruct también sabe cómo mapear listas automáticamente
    List<ImageResponseDTO> ImageDTOtoList(List<Image> images);

}
