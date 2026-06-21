package com.emailgenerator.entity;

import com.emailgenerator.dto.Tone;
import lombok.Builder;
import lombok.Value;
@Value
@Builder
public class EmailContext {

    String emailContent;
    String subject;
    String senderName;
    Tone tone;
}
