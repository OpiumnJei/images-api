package com.greetingsapp.imagesapi.infra.errors;

// dto que reprensenta los datos que se retornaran en caso de lanzar un error
public record ErrorResponseDTO(
        String errorCode,
        String errorDetails
) {
}
