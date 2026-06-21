package com.emailgenerator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Groq API configuration loaded from environment variables.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "groq.api")
public class GroqProperties {

    private String key;
    private String url = "https://api.groq.com/openai/v1/chat/completions";
    private String model = "llama-3.3-70b-versatile";
}
