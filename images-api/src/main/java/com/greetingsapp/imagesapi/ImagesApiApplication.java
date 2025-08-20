package com.greetingsapp.imagesapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "Image Greetings API",
				version = "1.0.0",
				description = "API REST para la gestión de imágenes, temáticas y categorías de saludos."
		)
)
public class ImagesApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImagesApiApplication.class, args);
	}

}
