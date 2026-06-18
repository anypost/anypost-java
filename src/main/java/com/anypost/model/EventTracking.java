package com.anypost.model;

/**
 * Tracking metadata on {@code email.opened} / {@code email.clicked} events,
 * mirroring the webhook payload's {@code data.tracking}. The {@code bot} is set
 * only when the interaction came from a mailbox image proxy; a human open or
 * click has no {@code bot}.
 *
 * @param bot the proxy classification, or {@code null} on a human open/click
 */
public record EventTracking(EventBot bot) {}
