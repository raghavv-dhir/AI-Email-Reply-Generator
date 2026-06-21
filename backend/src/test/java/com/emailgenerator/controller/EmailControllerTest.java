package com.emailgenerator.controller;

import com.emailgenerator.config.AppProperties;
import com.emailgenerator.dto.EmailRequest;
import com.emailgenerator.dto.EmailResponse;
import com.emailgenerator.dto.Tone;
import com.emailgenerator.exception.GlobalExceptionHandler;
import com.emailgenerator.service.EmailGeneratorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
@Import(GlobalExceptionHandler.class)
@EnableConfigurationProperties(AppProperties.class)
@TestPropertySource(properties = {
        "groq.api.key=test-key",
        "groq.api.url=http://localhost:9999/v1/chat/completions"
})
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailGeneratorService emailGeneratorService;

    @Test
    void generateReply_returnsOkWithGeneratedReply() throws Exception {
        EmailRequest request = EmailRequest.builder()
                .emailContent("Hello, can we meet tomorrow?")
                .subject("Meeting")
                .senderName("John")
                .tone(Tone.PROFESSIONAL)
                .build();

        when(emailGeneratorService.generateReply(any(EmailRequest.class)))
                .thenReturn(EmailResponse.builder().generatedReply("Sure, that works for me.").build());

        mockMvc.perform(post("/api/v1/email/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedReply").value("Sure, that works for me."));
    }

    @Test
    void generateReply_returnsBadRequestWhenContentMissing() throws Exception {
        EmailRequest request = EmailRequest.builder()
                .tone(Tone.PROFESSIONAL)
                .build();

        mockMvc.perform(post("/api/v1/email/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.emailContent").exists());
    }

    @Test
    void generateReply_returnsBadRequestWhenToneMissing() throws Exception {
        EmailRequest request = EmailRequest.builder()
                .emailContent("Hello")
                .build();

        mockMvc.perform(post("/api/v1/email/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.tone").exists());
    }
}
