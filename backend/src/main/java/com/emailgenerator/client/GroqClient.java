package com.emailgenerator.client;

import com.emailgenerator.config.GroqProperties;
import com.emailgenerator.exception.AiGenerationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Client for communicating with the Groq OpenAI-compatible chat completions API.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroqClient {

    private final WebClient webClient;
    private final GroqProperties groqProperties;
    private final ObjectMapper objectMapper;

    /**
     * Sends a prompt to Groq and returns the generated text response.
     *
     * @param prompt the fully constructed prompt
     * @return generated reply text
     */
    public String generateContent(String prompt) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", groqProperties.getModel());
        requestBody.put("temperature", 0.7);

        ArrayNode messages = requestBody.putArray("messages");
        messages.addObject()
                .put("role", "user")
                .put("content", prompt);

        try {
            String response = webClient.post()
                    .uri(groqProperties.getUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqProperties.getKey())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractGeneratedText(response);
        } catch (WebClientResponseException ex) {
            log.error("Groq API returned status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new AiGenerationException("Groq API request failed", ex);
        } catch (AiGenerationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error calling Groq API", ex);
            throw new AiGenerationException("Failed to communicate with Groq API", ex);
        }
    }

    private String extractGeneratedText(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new AiGenerationException("Groq returned no choices");
            }

            JsonNode content = choices.get(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new AiGenerationException("Groq returned an empty response");
            }

            return content.asText().trim();
        } catch (AiGenerationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiGenerationException("Failed to parse Groq response", ex);
        }
    }
}
