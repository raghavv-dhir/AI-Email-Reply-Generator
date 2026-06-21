package com.emailgenerator.client;

import com.emailgenerator.config.GroqProperties;
import com.emailgenerator.exception.AiGenerationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroqClientTest {

    private MockWebServer mockWebServer;
    private GroqClient groqClient;
    private GroqProperties groqProperties;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        groqProperties = new GroqProperties();
        groqProperties.setKey("gsk-test-key");
        groqProperties.setModel("llama-3.3-70b-versatile");
        groqProperties.setUrl(mockWebServer.url("/v1/chat/completions").toString());

        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .build();

        groqClient = new GroqClient(webClient, groqProperties, new ObjectMapper());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void generateContent_returnsExtractedText() throws InterruptedException {
        String responseBody = """
                {
                  "choices": [{
                    "message": {
                      "content": "Generated reply text"
                    }
                  }]
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        String result = groqClient.generateContent("Write a reply");

        assertThat(result).isEqualTo("Generated reply text");
        var request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer gsk-test-key");
        assertThat(request.getBody().readUtf8()).contains("llama-3.3-70b-versatile");
    }

    @Test
    void generateContent_throwsWhenNoChoices() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"choices\": []}")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> groqClient.generateContent("prompt"))
                .isInstanceOf(AiGenerationException.class)
                .hasMessageContaining("no choices");
    }

    @Test
    void generateContent_throwsOnApiError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> groqClient.generateContent("prompt"))
                .isInstanceOf(AiGenerationException.class);
    }
}
