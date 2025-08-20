package com.greetingsapp.imagesapi.domain.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

//@Component
public class PasswordEncoderUtil implements CommandLineRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String rawPassword = "admin_password_123"; // La contraseña que quieres usar
        String encodedPassword = passwordEncoder.encode(rawPassword);

        System.out.println("\n\n--- CONTRASEÑA ENCRIPTADA ---");
        System.out.println(encodedPassword);
        System.out.println("--- COPIA ESTA CONTRASEÑA ---\n\n");
    }
}
