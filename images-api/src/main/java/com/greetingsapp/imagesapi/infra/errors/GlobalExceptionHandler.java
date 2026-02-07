package com.greetingsapp.imagesapi.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

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

    //bad request(400) error del lado del cliente, para que las validaciones funcionen en el dto
    @ExceptionHandler(MethodArgumentNotValidException.class)//clase de la excepcion
    public ResponseEntity<Map<String, String>> tratarError400(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult() //obtiene un objeto que contiene los errores de validación lanzados por la excepcion
                .getFieldErrors() //lista de errores obtenidos
                .forEach(error ->
                        errors.put(error.getField(), //nombre del campo donde fallo la validacion
                                error.getDefaultMessage()) //mensaje de error del campo asociado
                );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors); // se retorna un 400, y el mapa de errores
    }

}
