package com.emailgenerator.service;

import com.emailgenerator.client.GroqClient;
import com.emailgenerator.dto.EmailRequest;
import com.emailgenerator.dto.EmailResponse;
import com.emailgenerator.dto.Tone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailGeneratorServiceTest {

    @Mock
    private GroqClient groqClient;

    @Mock
    private PromptBuilderService promptBuilderService;

    @InjectMocks
    private EmailGeneratorService emailGeneratorService;

    private EmailRequest request;

    @BeforeEach
    void setUp() {
        request = EmailRequest.builder()
                .emailContent("Can we schedule a meeting next week?")
                .subject("Meeting Request")
                .senderName("Jane Doe")
                .tone(Tone.PROFESSIONAL)
                .build();
    }

    @Test
    void generateReply_returnsGeneratedText() {
        when(promptBuilderService.buildPrompt(org.mockito.ArgumentMatchers.any())).thenReturn("prompt");
        when(groqClient.generateContent("prompt")).thenReturn("Thank you for reaching out.");

        EmailResponse response = emailGeneratorService.generateReply(request);

        assertThat(response.getGeneratedReply()).isEqualTo("Thank you for reaching out.");
        verify(promptBuilderService).buildPrompt(org.mockito.ArgumentMatchers.any());
        verify(groqClient).generateContent("prompt");
    }

    @Test
    void generateReply_passesContextToPromptBuilder() {
        when(promptBuilderService.buildPrompt(org.mockito.ArgumentMatchers.any())).thenReturn("built-prompt");
        when(groqClient.generateContent(anyString())).thenReturn("Reply body");

        emailGeneratorService.generateReply(request);

        verify(promptBuilderService).buildPrompt(org.mockito.ArgumentMatchers.argThat(context ->
                context.getEmailContent().equals(request.getEmailContent())
                        && context.getSubject().equals(request.getSubject())
                        && context.getSenderName().equals(request.getSenderName())
                        && context.getTone() == Tone.PROFESSIONAL
        ));
    }
}
