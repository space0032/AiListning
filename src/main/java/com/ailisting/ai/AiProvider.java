package com.ailisting.ai;

import java.util.Map;

/**
 * Abstraction layer for AI providers.
 *
 * WHY THIS PATTERN?
 * - Strategy Pattern: Each AI provider (Ollama, OpenAI, Claude) implements this interface.
 * - Dependency Inversion: Business logic depends on this abstraction, not on Ollama directly.
 * - Open/Closed Principle: Adding a new provider = add a new class, zero changes to existing code.
 *
 * REPLACING THE AI PROVIDER:
 * 1. Create a class implementing AiProvider (e.g., OpenAiProvider)
 * 2. Add provider-specific config to application.yml
 * 3. Create a @Configuration class with @ConditionalOnProperty to activate it
 * 4. Done. No existing code changes.
 */
public interface AiProvider {

    /**
     * Send a prompt to the AI and return the raw text response.
     *
     * @param prompt The full prompt string
     * @param parameters Optional parameters (temperature, maxTokens, etc.)
     * @return The AI's text response
     * @throws AiProviderException if the request fails
     */
    String generate(String prompt, Map<String, Object> parameters);

    /**
     * Send a prompt and request structured JSON response.
     * Providers that support JSON mode will use it.
     * Others will get JSON via prompt engineering.
     *
     * @param prompt The full prompt string
     * @param parameters Optional parameters
     * @return The AI's response as a JSON string
     * @throws AiProviderException if the request fails
     */
    String generateJson(String prompt, Map<String, Object> parameters);

    /**
     * @return Provider name (e.g., "ollama", "openai", "claude")
     */
    String getProviderName();

    /**
     * @return Model name (e.g., "qwen3.5:0.8b", "gpt-4")
     */
    String getModelName();

    /**
     * Check if the provider is available and responding.
     */
    boolean isAvailable();
}
