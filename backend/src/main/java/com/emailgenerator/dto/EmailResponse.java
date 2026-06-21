package com.emailgenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response containing the generated email reply.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {

    private String generatedReply;
}
