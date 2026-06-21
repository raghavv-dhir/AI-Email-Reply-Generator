package com.emailgenerator.config;

import com.emailgenerator.exception.ConfigurationValidationException;
import com.emailgenerator.util.LogMaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
@Slf4j
@Component
@RequiredArgsConstructor
public class GroqConfigValidator {

    private final GroqProperties groqProperties;
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        if (!StringUtils.hasText(groqProperties.getKey())) {
            throw new ConfigurationValidationException("GROQ_API_KEY environment variable is not configured");
        }
        if (!StringUtils.hasText(groqProperties.getUrl())) {
            throw new ConfigurationValidationException("GROQ_API_URL environment variable is not configured");
        }

        log.info("Groq configuration validated successfully");
        log.info("Groq API URL configured: {}", groqProperties.getUrl());
        log.info("Groq model configured: {}", groqProperties.getModel());
        log.info("Groq API key configured: {}", LogMaskingUtil.maskApiKey(groqProperties.getKey()));
    }
}
