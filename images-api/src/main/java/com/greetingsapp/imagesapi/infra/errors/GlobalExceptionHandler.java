package com.greetingsapp.imagesapi.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice// esta anotacion permite manejar excepciones de manera global en todos los controladores
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex) {
        //errorResponse contiene la respuesta en formato json, que se retornara a al user
        var errorResponse = new ErrorResponseDTO("RESOURCE_NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND); //se retorna el statusCode 404
    }


    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateResource(DuplicateResourceException ex) {
        var errorResponse = new ErrorResponseDTO("RESOURCE_CONFLICT", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT); // 409
    }

}
