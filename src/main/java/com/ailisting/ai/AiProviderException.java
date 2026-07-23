package com.ailisting.ai;

public class AiProviderException extends RuntimeException {

    private final String provider;
    private final String model;

    public AiProviderException(String provider, String model, String message) {
        super(String.format("[%s/%s] %s", provider, model, message));
        this.provider = provider;
        this.model = model;
    }

    public AiProviderException(String provider, String model, String message, Throwable cause) {
        super(String.format("[%s/%s] %s", provider, model, message), cause);
        this.provider = provider;
        this.model = model;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }
}
