package com.saikvt.event.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Exhibition Management API").version("1.0"))
                .tags(Arrays.asList(
                        new Tag().name("User Profile").description("APIs to manage user profiles"),
                        new Tag().name("Exhibition").description("APIs to manage exhibitions"),
                        new Tag().name("Module").description("APIs to manage modules"),
                        new Tag().name("Stalls").description("APIs to manage stalls"),
                        new Tag().name("Poster Content").description("APIs to manage poster content"),
                        new Tag().name("Quiz Result").description("APIs to manage quiz results"),
                        new Tag().name("Feedback").description("APIs to manage feedbacks")
                ));
    }
}

