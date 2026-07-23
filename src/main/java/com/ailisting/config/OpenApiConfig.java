package com.ailisting.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080/api/v1").description("Local Development")
                ))
                .tags(List.of(
                        new Tag().name("Auth").description("Authentication & Authorization"),
                        new Tag().name("Listings").description("Product Listing CRUD"),
                        new Tag().name("AI").description("AI Generation"),
                        new Tag().name("Users").description("User Profile"),
                        new Tag().name("Cache").description("Cache Management (Admin)")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(securityComponents());
    }

    private Info apiInfo() {
        return new Info()
                .title("AI E-commerce Listing Generator API")
                .description("""
                        Production-grade AI SaaS for generating SEO-optimized product listings.
                        
                        ## Features
                        - JWT Authentication with Refresh Tokens
                        - AI-powered listing generation using Ollama
                        - Multi-platform support (Amazon, Flipkart, Meesho, Shopify)
                        - Image upload to MinIO
                        - Redis caching
                        - Rate limiting
                        
                        ## Authentication
                        1. Register or Login to get tokens
                        2. Use the `accessToken` in `Authorization: Bearer <token>` header
                        3. Use `refreshToken` to get new access tokens
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("AI Listing Generator Team")
                        .email("support@ailisting.com"))
                .license(new License()
                        .name("MIT")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .bearerFormat("JWT")
                                .scheme("bearer")
                                .description("Enter JWT token"));
    }
}