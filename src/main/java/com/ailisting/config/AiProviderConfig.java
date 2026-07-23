package com.ailisting.config;

import com.ailisting.ai.AiProviderFactory;
import com.ailisting.ai.OllamaProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * AI Provider configuration.
 *
 * WHY @Configuration here?
 * - Wires Ollama provider at startup
 * - Registers it with the factory
 * - Future: Add OpenAI, Claude providers with @ConditionalOnProperty
 *
 * TO ADD A NEW PROVIDER:
 * 1. Create class implementing AiProvider
 * 2. Add @Bean method here with @ConditionalOnProperty
 * 3. Add provider config to application.yml
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AiProviderConfig {

    @Value("${app.ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${app.ollama.model}")
    private String ollamaModel;

    @Value("${app.ollama.timeout}")
    private long ollamaTimeout;

    @Bean
    public OllamaProvider ollamaProvider(WebClient ollamaWebClient, AiProviderFactory factory) {
        OllamaProvider provider = new OllamaProvider(ollamaWebClient, ollamaModel, ollamaTimeout);
        factory.registerProvider(provider);
        log.info("Ollama provider initialized: model={}, baseUrl={}", ollamaModel, ollamaBaseUrl);
        return provider;
    }
}
