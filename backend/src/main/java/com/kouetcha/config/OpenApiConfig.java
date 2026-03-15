package com.kouetcha.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Value("${server.port:9020}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tasks Manager")
                        .description("""
                            API complète pour la gestion de tâches et projets.
                            
                            Fonctionnalités principales :
                            - Création, lecture, mise à jour et suppression de tâches (CRUD)
                            - Gestion des projets et des utilisateurs
                            - Assignation et suivi de l'avancement des tâches
                            - Notifications et rappels
                            
                            Technologies : Spring Boot, Angular, JPA/Hibernate, JWT pour sécurité
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Équipe Tasks Manager")
                                .email("support@tasksmanager.local")
                                .url("http://localhost:" + serverPort))
                        .license(new License()
                                .name("Licence Interne")
                                .url("http://localhost:" + serverPort + "/license")))

                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Jeton d'authentification JWT"))
                )
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}