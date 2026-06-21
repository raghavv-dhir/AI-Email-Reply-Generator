package com.emailgenerator;

import com.emailgenerator.config.GroqConfigValidator;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmailGeneratorIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GroqConfigValidator groqConfigValidator;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) throws IOException {
        if (mockWebServer == null) {
            mockWebServer = new MockWebServer();
            mockWebServer.start();
        }
        registry.add("groq.api.url", () -> mockWebServer.url("/v1/chat/completions").toString());
        registry.add("groq.api.key", () -> "integration-test-key");
    }

    @AfterAll
    static void stopMockServer() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @BeforeEach
    void validateConfig() {
        groqConfigValidator.validateConfiguration();
    }

    @Test
    void healthEndpoint_isAvailable() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void generateEndpoint_returnsGeneratedReply() throws Exception {
        String groqResponse = """
                {
                  "choices": [{
                    "message": {
                      "content": "Thank you for your email. I will follow up shortly."
                    }
                  }]
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(groqResponse)
                .addHeader("Content-Type", "application/json"));

        String requestBody = """
                {
                  "emailContent": "Can you send the report by Friday?",
                  "subject": "Report Request",
                  "senderName": "Sarah",
                  "tone": "PROFESSIONAL"
                }
                """;

        mockMvc.perform(post("/api/v1/email/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedReply")
                        .value("Thank you for your email. I will follow up shortly."));
    }
}
