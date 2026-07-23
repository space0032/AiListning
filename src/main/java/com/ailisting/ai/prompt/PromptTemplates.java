package com.ailisting.ai.prompt;

/**
 * Prompt templates for AI listing generation.
 *
 * WHY SEPARATE CLASS?
 * - Prompts are business logic, not code logic
 * - Easy to iterate on prompts without recompiling
 * - Future: Load from database/files for admin prompt management
 *
 * SMALL MODEL OPTIMIZATION:
 * - Keep prompts short and direct (0.8B models have small context windows)
 * - Use simple, clear instructions
 * - Provide structure via examples
 * - Request JSON format explicitly
 */
public final class PromptTemplates {

    private PromptTemplates() {}

    // ===========================
    // LISTING GENERATION
    // ===========================

    public static final String LISTING_GENERATION = """
            Generate an e-commerce product listing for the following product.
            
            Platform: {platform}
            Product Name: {productName}
            Category: {category}
            Brand: {brand}
            Material: {material}
            Color: {color}
            Size: {size}
            Description: {productDescription}
            
            Generate the following fields as JSON:
            {{
              "seoTitle": "SEO optimized title (max 200 chars)",
              "bulletPoints": "5-7 bullet points, each on a new line starting with •",
              "description": "Detailed product description (200-300 words)",
              "tags": "comma separated tags (10-15 tags)",
              "keywords": "comma separated search keywords (15-20 keywords)",
              "metaDescription": "Meta description for SEO (max 160 chars)",
              "platformFormattedListing": "Complete listing formatted for {platform}"
            }}
            
            Rules:
            - Use simple, clear language
            - Highlight key features and benefits
            - Include relevant keywords naturally
            - Format specifically for {platform} guidelines
            """;

    // ===========================
    // PLATFORM-SPECIFIC FORMATTING
    // ===========================

    public static final String AMAZON_FORMAT = """
            Format this listing for Amazon:
            - Title: Brand + Product + Key Feature + Size/Color (max 200 chars)
            - 5 Bullet Points starting with capital letter
            - Description: 200-300 words, HTML formatted
            - Backend keywords (no title/brand words)
            """;

    public static final String FLIPKART_FORMAT = """
            Format this listing for Flipkart:
            - Title: Product Name + Key Spec + Color + Size
            - Highlights: 4-6 key points
            - Description: Detailed with specifications
            - Focus on value proposition
            """;

    public static final String MEESHO_FORMAT = """
            Format this listing for Meesho:
            - Short, catchy title
            - Focus on price value
            - Simple language
            - Key features in bullet points
            - Target budget-conscious buyers
            """;

    public static final String SHOPIFY_FORMAT = """
            Format this listing for Shopify:
            - SEO-friendly title
            - Compelling product description
            - Feature-benefit format
            - Call-to-action focused
            """;

    // ===========================
    // HELPER METHODS
    // ===========================

    public static String buildListingPrompt(
            String platform,
            String productName,
            String category,
            String brand,
            String material,
            String color,
            String size,
            String productDescription) {

        return LISTING_GENERATION
                .replace("{platform}", platform != null ? platform : "General")
                .replace("{productName}", productName != null ? productName : "")
                .replace("{category}", category != null ? category : "")
                .replace("{brand}", brand != null ? brand : "")
                .replace("{material}", material != null ? material : "")
                .replace("{color}", color != null ? color : "")
                .replace("{size}", size != null ? size : "")
                .replace("{productDescription}", productDescription != null ? productDescription : "");
    }

    public static String getPlatformFormat(String platform) {
        if (platform == null) return "";
        return switch (platform.toUpperCase()) {
            case "AMAZON" -> AMAZON_FORMAT;
            case "FLIPKART" -> FLIPKART_FORMAT;
            case "MEESHO" -> MEESHO_FORMAT;
            case "SHOPIFY" -> SHOPIFY_FORMAT;
            default -> "";
        };
    }
}
