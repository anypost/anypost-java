package com.anypost.model;

import java.util.Map;

/**
 * One event inside a {@link WebhookDelivery}.
 *
 * @param id         the unique event id; stable across retries &mdash; de-duplicate on it
 * @param type       a webhook event type or {@code "webhook.test"}
 * @param occurredAt the ISO 8601 timestamp when the event occurred
 * @param data       always carries {@code email_id}; the rest depends on the event type
 */
public record WebhookDeliveryEvent(
        String id,
        String type,
        String occurredAt,
        Map<String, Object> data) {}
