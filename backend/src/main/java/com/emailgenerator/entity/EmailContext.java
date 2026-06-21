package com.emailgenerator.entity;

import com.emailgenerator.dto.Tone;
import lombok.Builder;
import lombok.Value;

/**
 * Internal domain representation of email context used for prompt building.
 */
@Value
@Builder
public class EmailContext {

    String emailContent;
    String subject;
    String senderName;
    Tone tone;
}
