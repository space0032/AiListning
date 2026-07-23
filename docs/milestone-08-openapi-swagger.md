# Milestone 8: OpenAPI & Swagger Documentation

## Overview
Implement comprehensive API documentation using OpenAPI 3.0 with Swagger UI for interactive exploration.

## Implementation

### Dependencies
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### OpenApiConfig
```java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("AI Listing API")
                .version("1.0.0")
                .description("E-commerce listing generation platform with AI integration")
                .contact(new Contact()
                    .name("API Support")
                    .email("support@ailisting.com")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token for authentication")));
    }
    
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/api/public/**")
            .build();
    }
    
    @Bean
    public GroupedOpenApi authenticatedApi() {
        return GroupedOpenApi.builder()
            .group("authenticated")
            .pathsToMatch("/api/listings/**", "/api/ai/**")
            .build();
    }
    
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
            .group("admin")
            .pathsToMatch("/api/admin/**")
            .build();
    }
}
```

### @Tag/@Operation Annotations
```java
@RestController
@RequestMapping("/listings")
@Tag(name = "Listings", description = "Product listing management")
public class ListingController {
    
    @Operation(
        summary = "Create a new listing",
        description = "Create a product listing with initial details",
        responses = {
            @ApiResponse(responseCode = "201", description = "Listing created",
                content = @Content(schema = @Schema(implementation = ListingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
        }
    )
    @PostMapping
    public ResponseEntity<ListingResponse> createListing(
            @Valid @RequestBody ListingRequest request) {
        // Implementation
    }
    
    @Operation(
        summary = "Get listing by ID",
        description = "Retrieve a single listing with all details"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getListing(
            @Parameter(description = "Listing ID") @PathVariable Long id) {
        // Implementation
    }
    
    @Operation(
        summary = "Search listings",
        description = "Search across product name, description, brand, and category"
    )
    @GetMapping("/search")
    public ResponseEntity<Page<ListingResponse>> searchListings(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        // Implementation
    }
}

@RestController
@RequestMapping("/ai")
@Tag(name = "AI Generation", description = "AI-powered listing content generation")
public class AiController {
    
    @Operation(
        summary = "Generate listing content",
        description = "Use AI to generate SEO-optimized listing content",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/generate-listing")
    public ResponseEntity<ListingGenerationResponse> generateListing(
            @Valid @RequestBody ListingGenerationRequest request) {
        // Implementation
    }
}
```

### application.yml Configuration
```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui/index.html
    operationsSorter: method
    tagsSorter: alpha
    docExpansion: none
  api-docs:
    path: /api-docs
  paths-to-match:
    - /api/**
  packages-to-scan:
    - com.ailisting.controller
```

## Architecture Decisions

### Why springdoc-openapi?
- **Modern**: Actively maintained, supports Spring Boot 3.x
- **Automatic**: Scans controllers and generates OpenAPI spec
- **Interactive**: Swagger UI for testing and exploration
- **Standards**: OpenAPI 3.0 specification

### Grouping Strategy
- **Public**: Unauthenticated endpoints for documentation clarity
- **Authenticated**: Core business logic requiring authentication
- **Admin**: Administrative operations (hidden by default)

### Security Scheme
- **JWT Bearer**: Standard authentication method
- **Global Security**: Applies to all endpoints unless overridden
- **Swagger UI Integration**: Token input field in UI

## Testing

### Access Documentation
```bash
# Swagger UI
http://localhost:8080/swagger-ui/index.html

# OpenAPI JSON
http://localhost:8080/api-docs

# OpenAPI YAML
http://localhost:8080/api-docs.yaml
```

### Verify Documentation Quality
1. All endpoints documented with descriptions
2. Request/response schemas generated correctly
3. Authentication requirements shown
4. Error responses documented

### Export Documentation
```bash
# Generate client SDK
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/api-docs \
  -g java \
  -o ./client-sdk
```

## Next Steps
- Add request/response examples
- Implement API versioning documentation
- Add rate limit documentation to OpenAPI spec
- Generate client libraries automatically