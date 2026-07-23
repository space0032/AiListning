package com.ailisting.model.dto.request;

import com.ailisting.model.enums.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingGenerationRequest {

    @NotBlank(message = "Product name is required")
    private String productName;

    private String productDescription;

    private String category;

    private String brand;

    private String material;

    private String color;

    private String size;

    @NotNull(message = "Platform is required")
    private Platform platform;
}