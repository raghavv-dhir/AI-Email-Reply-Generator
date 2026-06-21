package com.emailgenerator.service;

import com.emailgenerator.client.GroqClient;
import com.emailgenerator.dto.EmailRequest;
import com.emailgenerator.dto.EmailResponse;
import com.emailgenerator.entity.EmailContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailGeneratorService {

    private final GroqClient groqClient;
    private final PromptBuilderService promptBuilderService;
    public EmailResponse generateReply(EmailRequest request) {
        log.info("Generating email reply with tone: {}", request.getTone());

        EmailContext context = EmailContext.builder()
                .emailContent(request.getEmailContent())
                .subject(request.getSubject())
                .senderName(request.getSenderName())
                .tone(request.getTone())
                .build();

        String prompt = promptBuilderService.buildPrompt(context);
        String generatedReply = groqClient.generateContent(prompt);

        log.info("Successfully generated email reply ({} characters)", generatedReply.length());

        return EmailResponse.builder()
                .generatedReply(generatedReply)
                .build();
    }
}
