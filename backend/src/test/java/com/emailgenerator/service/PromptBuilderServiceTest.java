package com.emailgenerator.service;

import com.emailgenerator.dto.Tone;
import com.emailgenerator.entity.EmailContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderServiceTest {

    private final PromptBuilderService promptBuilderService = new PromptBuilderService();

    @Test
    void buildPrompt_includesToneSubjectSenderAndContent() {
        EmailContext context = EmailContext.builder()
                .emailContent("Please review the attached proposal.")
                .subject("Q4 Proposal")
                .senderName("Alex Smith")
                .tone(Tone.FORMAL)
                .build();

        String prompt = promptBuilderService.buildPrompt(context);

        assertThat(prompt).contains("FORMAL");
        assertThat(prompt).contains("Q4 Proposal");
        assertThat(prompt).contains("Alex Smith");
        assertThat(prompt).contains("Please review the attached proposal.");
        assertThat(prompt).contains("Do not invent facts");
    }

    @Test
    void buildPrompt_handlesMissingOptionalFields() {
        EmailContext context = EmailContext.builder()
                .emailContent("Hello there")
                .tone(Tone.CASUAL)
                .build();

        String prompt = promptBuilderService.buildPrompt(context);

        assertThat(prompt).contains("Not provided");
        assertThat(prompt).contains("CASUAL");
    }
}
