package com.emailgenerator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Incoming request to generate an AI email reply.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NotBlank(message = "Email content is required")
    @Size(max = 50000, message = "Email content must not exceed 50000 characters")
    private String emailContent;

    @Size(max = 500, message = "Subject must not exceed 500 characters")
    private String subject;

    @Size(max = 200, message = "Sender name must not exceed 200 characters")
    private String senderName;

    @NotNull(message = "Tone is required")
    private Tone tone;
}
