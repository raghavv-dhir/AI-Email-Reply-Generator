package com.emailgenerator.service;

import com.emailgenerator.dto.Tone;
import com.emailgenerator.entity.EmailContext;
import com.emailgenerator.util.PromptConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
@Component
public class PromptBuilderService {
    public String buildPrompt(EmailContext context) {
        String toneDescription = describeTone(context.getTone());
        String subject = StringUtils.hasText(context.getSubject()) ? context.getSubject() : "Not provided";
        String sender = StringUtils.hasText(context.getSenderName()) ? context.getSenderName() : "Not provided";

        return PromptConstants.SYSTEM_INSTRUCTION + """

                ---
                Requested Tone: %s (%s)

                Email Subject: %s
                Sender Name: %s

                Email Thread / Latest Message:
                %s
                ---

                Draft a reply to the latest message in the thread above.
                Match the requested tone precisely.
                Do not include a subject line or signature unless one appears necessary from context.
                """.formatted(
                context.getTone().name(),
                toneDescription,
                subject,
                sender,
                context.getEmailContent()
        );
    }

    private String describeTone(Tone tone) {
        return switch (tone) {
            case PROFESSIONAL -> "Clear, respectful, and business-appropriate";
            case FRIENDLY -> "Warm, approachable, and personable while remaining professional";
            case CASUAL -> "Relaxed and conversational, suitable for informal contacts";
            case FORMAL -> "Highly formal with proper salutations and structured language";
            case CONCISE -> "Brief and to the point with no unnecessary filler";
            case DETAILED -> "Thorough and comprehensive, covering all relevant points";
            case PERSUASIVE -> "Compelling and action-oriented to influence the recipient";
            case APOLOGETIC -> "Sincere, empathetic, and accountable";
            case THANK_YOU -> "Grateful and appreciative in tone";
            case FOLLOW_UP -> "Polite reminder or check-in on a previous conversation";
        };
    }
}
