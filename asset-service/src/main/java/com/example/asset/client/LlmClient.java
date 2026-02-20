package com.example.asset.client;

/**
 * Client for LLM APIs (OpenAI-compatible: OpenAI, Azure OpenAI, Ollama, etc.).
 * Used by the document extraction agent to get structured asset data from document text.
 */
public interface LlmClient {

    /**
     * Send a prompt to the LLM and return the raw text response.
     *
     * @param userPrompt the user message (e.g. document text + extraction instructions)
     * @return the model's reply text, or null if disabled/failed
     */
    String complete(String userPrompt);
}
