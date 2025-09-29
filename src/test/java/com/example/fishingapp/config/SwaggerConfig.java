package com.example.fishingapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Arroyo Fishing App API")
                        .version("1.0")
                        .description("API REST para el proyecto de pesca")
                        .contact(new Contact()
                                .name("Imanol Hdez")
                                .email("tu-email@ejemplo.com")
                        )
                );
    }
}
