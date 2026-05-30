package com.anypost.model;

/**
 * A suppressed recipient address, scoped to a topic.
 *
 * @param id             the {@code sup_}-prefixed id, for log correlation
 * @param email          the suppressed address, normalized to lowercase
 * @param topic          the topic this applies to; {@code "*"} means every topic
 * @param reason         why the address is suppressed
 * @param origin         the provenance of the row
 * @param classification a bounce classification or ARF feedback-type, or {@code null} for manual entries
 * @param smtpCode       the SMTP reply code from the bounce, or {@code null}
 * @param note           a free-form note attached at creation, or {@code null}
 * @param suppressedAt   when the suppression was first observed
 * @param expiresAt      when it stops applying, or {@code null} for never
 * @param createdAt      the creation timestamp
 */
public record Suppression(
        String id,
        String email,
        String topic,
        SuppressionReason reason,
        SuppressionOrigin origin,
        String classification,
        Integer smtpCode,
        String note,
        String suppressedAt,
        String expiresAt,
        String createdAt) {}
