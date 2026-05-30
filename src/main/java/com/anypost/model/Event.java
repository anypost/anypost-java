package com.anypost.model;

import java.util.List;

/**
 * A single email-pipeline event for the team. Every field is always present;
 * fields that don't apply to a given event type are {@code null} (or empty)
 * rather than absent.
 *
 * @param id                   the stable id for log correlation (not addressable)
 * @param type                 the event type
 * @param occurredAt           the ISO 8601 UTC timestamp when the event was observed
 * @param emailId              the {@code email_<uuidv7>} id minted when the message was accepted, or {@code null}
 * @param messageId            the RFC 5322 {@code Message-ID:} header, or {@code null}
 * @param from                 the envelope {@code From:} address, or {@code null}
 * @param fromDomain           the {@code From:} domain, lowercased, or {@code null}
 * @param recipient            the single recipient this event refers to, or {@code null}
 * @param subject              the captured {@code Subject:} header, or {@code null}
 * @param campaign             the originating send's campaign value, or {@code null}
 * @param templateId           the template the originating send used, or {@code null}
 * @param topic                the send-time topic, or {@code null}
 * @param tags                 the customer-supplied tags from the originating send
 * @param smtpCode             the SMTP reply code observed, or {@code null}
 * @param bounceType           the bounce type (only on {@code email.bounced}), or {@code null}
 * @param bounceClassification the bounce classification (only on {@code email.bounced}), or {@code null}
 * @param attempt              the delivery attempt number, or {@code null} for non-delivery events
 */
public record Event(
        String id,
        EventType type,
        String occurredAt,
        String emailId,
        String messageId,
        String from,
        String fromDomain,
        String recipient,
        String subject,
        String campaign,
        String templateId,
        String topic,
        List<String> tags,
        Integer smtpCode,
        String bounceType,
        String bounceClassification,
        Integer attempt) {}
