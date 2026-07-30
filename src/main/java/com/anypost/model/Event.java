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
 * @param ipPool               which dedicated IP pool the message egressed from, or {@code null}
 *                             on sends that named no pool and on accounts without dedicated IPs;
 *                             set on every event for the message, not just {@code email.sent}
 * @param smtpCode             the SMTP reply code observed, or {@code null}
 * @param bounceType           why the message failed (only on {@code email.bounced}), or
 *                             {@code null}. One of {@code permanent} (the receiver refused the
 *                             address outright; suppressed, and what counts against list
 *                             quality), {@code transient} (a temporary failure still unresolved
 *                             when the message was reported), or {@code expired} (aged out of
 *                             the retry queue after 72 hours without reaching the receiver;
 *                             not a hard bounce, and not suppressed)
 * @param bounceClassification the bounce classification (only on {@code email.bounced}), or {@code null}
 * @param attempt              the delivery attempt number, or {@code null} for non-delivery events
 * @param tracking             tracking metadata mirroring the webhook payload's {@code data.tracking};
 *                             {@code null} except on opens/clicks, and on human opens/clicks
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
        String ipPool,
        Integer smtpCode,
        String bounceType,
        String bounceClassification,
        Integer attempt,
        EventTracking tracking) {}
