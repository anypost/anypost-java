package com.anypost.model;

import java.util.List;

/**
 * A webhook with its full signing secret. Returned only on create and
 * rotate-secret.
 *
 * @param id                          the {@code wh_}-prefixed id
 * @param name                        the human-readable name
 * @param url                         the {@code https://} delivery endpoint
 * @param events                      the subscribed event types
 * @param status                      the delivery state
 * @param signingSecretPrefix         the first 12 characters of the signing secret
 * @param signingSecretPreviousPrefix the prefix of the previous secret during a rotation grace window, or {@code null}
 * @param signingSecretGraceExpiresAt when the rotation grace window ends, or {@code null}
 * @param lastDeliveryAt              when a delivery was last attempted, or {@code null}
 * @param createdAt                   the creation timestamp
 * @param signingSecret               the full signing secret ({@code whsec_...}); returned once, store it securely
 */
public record WebhookWithSecret(
        String id,
        String name,
        String url,
        List<WebhookEventType> events,
        WebhookStatus status,
        String signingSecretPrefix,
        String signingSecretPreviousPrefix,
        String signingSecretGraceExpiresAt,
        String lastDeliveryAt,
        String createdAt,
        String signingSecret) {}
