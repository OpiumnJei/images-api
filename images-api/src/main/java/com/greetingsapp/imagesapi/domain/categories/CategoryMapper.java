package com.greetingsapp.imagesapi.domain.categories;

import com.greetingsapp.imagesapi.dto.categories.CategoryResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(source = "id", target = "categoryId")
    @Mapping(source = "name", target = "categoryName")
    CategoryResponseDTO categoryToCategoryResponseDTO(Category category);

    List<CategoryResponseDTO> categoryListToCategoryResponseDTOList(List<Category> categories);
}