package com.greetingsapp.imagesapi.infra.errors;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
