package com.email.generator.app;

import lombok.Data;

@Data
public class EmailRequest {
    private String emailContent;
    private String tone;
}