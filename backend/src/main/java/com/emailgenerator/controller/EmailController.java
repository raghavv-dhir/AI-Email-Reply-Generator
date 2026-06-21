package com.emailgenerator.controller;

import com.emailgenerator.dto.EmailRequest;
import com.emailgenerator.dto.EmailResponse;
import com.emailgenerator.service.EmailGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailGeneratorService emailGeneratorService;
    @PostMapping("/generate")
    public ResponseEntity<EmailResponse> generateReply(@Valid @RequestBody EmailRequest request) {
        log.debug("Received email generation request with tone: {}", request.getTone());
        EmailResponse response = emailGeneratorService.generateReply(request);
        return ResponseEntity.ok(response);
    }
}
