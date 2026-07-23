# Milestone 5: AI Integration with Ollama

## Goal
Implement AI-powered product listing generation with an abstraction layer that allows swapping AI providers without changing business logic.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    AI Abstraction Layer                   │
│  ┌───────────────────────────────────────────────────┐  │
│  │               AiProvider (Interface)               │  │
│  │  - generate(prompt, params) → String               │  │
│  │  - generateJson(prompt, params) → String           │  │
│  │  - getProviderName() → String                      │  │
│  │  - isAvailable() → boolean                         │  │
│  └───────────────────────────────────────────────────┘  │
│                           │                              │
│         ┌─────────────────┼─────────────────┐           │
│         ▼                 ▼                 ▼           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │OllamaProvider│  │OpenAiProvider│  │ClaudeProvider│    │
│  │ (qwen3.5)   │  │  (gpt-4)    │  │  (claude)   │    │
│  └─────────────┘  └─────────────┘  └─────────────┘    │
│                                                          │
│  ┌───────────────────────────────────────────────────┐  │
│  │            AiProviderFactory                        │  │
│  │  - registerProvider(provider)                       │  │
│  │  - getProvider("ollama") → AiProvider               │  │
│  │  - getDefaultProvider() → AiProvider                │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│               AiGenerationService                        │
│  - generateListing(request, userId) → ListingResponse    │
│  - isAiAvailable() → boolean                             │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                   AiController                           │
│  POST /ai/generate-listing                               │
│  GET  /ai/health                                         │
└─────────────────────────────────────────────────────────┘
```

## AI Provider Abstraction

### Why This Pattern?

**Strategy Pattern**: Each AI provider implements `AiProvider`. Business logic depends on the interface, not implementations.

**Dependency Inversion**: High-level modules (services) depend on abstractions (AiProvider), not low-level modules (OllamaProvider).

**Open/Closed Principle**: Adding a new provider = create a new class + register it. Zero changes to existing code.

### How to Add a New Provider

1. Create class implementing `AiProvider`:
```java
public class OpenAiProvider implements AiProvider {
    // Implement all methods
}
```

2. Add config to `application.yml`:
```yaml
app:
  openai:
    api-key: sk-...
    model: gpt-4
```

3. Create config bean with `@ConditionalOnProperty`:
```java
@Bean
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
public OpenAiProvider openAiProvider(...) { ... }
```

4. Done. No existing code changes.

## Prompt Templates

### Why Separate?
- Prompts are **business logic**, not code
- Easy to iterate without recompiling
- Future: Store in database for admin management

### Small Model Optimization (qwen3.5:0.8b)
- Short, direct prompts
- Simple instructions
- Explicit JSON format request
- Platform-specific formatting

### Template: Listing Generation
```
Generate an e-commerce product listing for the following product.

Platform: {platform}
Product Name: {productName}
Category: {category}
...

Generate the following fields as JSON:
{
  "seoTitle": "...",
  "bulletPoints": "...",
  "description": "...",
  "tags": "...",
  "keywords": "...",
  "metaDescription": "...",
  "platformFormattedListing": "..."
}
```

## Ollama API Integration

### Endpoint Used
`POST /api/generate` (not chat, simpler for small models)

### Request Format
```json
{
  "model": "qwen3.5:0.8b",
  "prompt": "...",
  "stream": false,
  "format": "json",
  "options": {
    "temperature": 0.7,
    "num_predict": 1024,
    "top_p": 0.9
  }
}
```

### Response Parsing
1. Extract `response` field from Ollama JSON
2. Find JSON object in response (may be in markdown code fences)
3. Validate JSON structure
4. Map to `ListingGenerationResponse`

## Generation Flow

```
Client                    Server                    Ollama
  │                         │                         │
  │ POST /ai/generate-listing│                         │
  │ {productName, platform} │                         │
  │────────────────────────>│                         │
  │                         │  Build prompt            │
  │                         │  from PromptTemplates    │
  │                         │                         │
  │                         │  POST /api/generate      │
  │                         │  {model, prompt, json}   │
  │                         │────────────────────────>│
  │                         │                         │
  │                         │  Generate response       │
  │                         │<────────────────────────│
  │                         │                         │
  │                         │  Parse JSON response     │
  │                         │  Save generation log     │
  │                         │                         │
  │  {seoTitle, bullets,    │                         │
  │   description, tags...} │                         │
  │<────────────────────────│                         │
```

## AI Generation Log

Every generation is logged to `ai_generation_logs` table:
- User ID
- Listing ID (if applicable)
- Model used
- Generation time (ms)
- Status (SUCCESS/FAILED/TIMEOUT)
- Error message (if failed)

**Purpose**: Analytics, billing (future), debugging, rate limiting.

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/ai/generate-listing` | Generate listing from product details |
| GET | `/ai/health` | Check if AI provider is available |
| POST | `/listings/{id}/generate` | Generate content for existing listing |

## Files Created/Modified

### New Files
- `src/main/java/com/ailisting/ai/AiProvider.java` - Interface
- `src/main/java/com/ailisting/ai/AiProviderException.java` - Custom exception
- `src/main/java/com/ailisting/ai/AiProviderFactory.java` - Provider registry
- `src/main/java/com/ailisting/ai/OllamaProvider.java` - Ollama implementation
- `src/main/java/com/ailisting/ai/AiGenerationService.java` - Service interface
- `src/main/java/com/ailisting/ai/AiGenerationServiceImpl.java` - Service impl
- `src/main/java/com/ailisting/ai/prompt/PromptTemplates.java` - Prompt templates
- `src/main/java/com/ailisting/config/AiProviderConfig.java` - Provider wiring
- `src/main/java/com/ailisting/controller/AiController.java` - REST endpoints
- `src/main/java/com/ailisting/model/dto/request/ListingGenerationRequest.java`
- `src/main/java/com/ailisting/model/dto/response/ListingGenerationResponse.java`

### Modified Files
- `ListingService.java` - Added generateListingContent
- `ListingServiceImpl.java` - AI integration
- `ListingController.java` - Added generate endpoint

## Replacing the AI Provider

To switch from Ollama to OpenAI:

1. Create `OpenAiProvider implements AiProvider`
2. Add config:
```yaml
app:
  openai:
    api-key: ${OPENAI_API_KEY}
    model: gpt-4
```
3. Create config bean with `@ConditionalOnProperty`
4. Update `application.yml`: `app.ai.provider: openai`
5. Restart application

**No changes to**: Service layer, controllers, entities, repositories.
