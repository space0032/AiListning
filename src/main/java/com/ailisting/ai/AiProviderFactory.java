package com.ailisting.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for managing AI providers.
 *
 * WHY FACTORY PATTERN?
 * - Centralizes provider creation and lookup
 * - Supports multiple providers running simultaneously
 * - Easy to add new providers without changing consumers
 *
 * USAGE:
 * AiProvider provider = factory.getProvider("ollama");
 * String response = provider.generate(prompt, params);
 */
@Slf4j
@Component
public class AiProviderFactory {

    private final Map<String, AiProvider> providers = new ConcurrentHashMap<>();

    public void registerProvider(AiProvider provider) {
        providers.put(provider.getProviderName(), provider);
        log.info("Registered AI provider: {} (model: {})", provider.getProviderName(), provider.getModelName());
    }

    public AiProvider getProvider(String name) {
        return Optional.ofNullable(providers.get(name))
                .orElseThrow(() -> new IllegalArgumentException("Unknown AI provider: " + name));
    }

    public AiProvider getDefaultProvider() {
        if (providers.isEmpty()) {
            throw new IllegalStateException("No AI providers registered");
        }
        // Return "ollama" if available, otherwise the first one
        return providers.getOrDefault("ollama", providers.values().iterator().next());
    }

    public Map<String, AiProvider> getAllProviders() {
        return Map.copyOf(providers);
    }

    public boolean hasProvider(String name) {
        return providers.containsKey(name);
    }
}
