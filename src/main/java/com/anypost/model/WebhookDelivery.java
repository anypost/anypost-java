package com.anypost.model;

import java.util.List;

/**
 * The outer envelope of a webhook delivery: one batch of one or more events.
 *
 * @param batchId   identifies this batch; stable across retries &mdash; de-duplicate on it
 * @param timestamp the Unix timestamp the batch was signed with
 * @param events    the events in this batch
 */
public record WebhookDelivery(String batchId, long timestamp, List<WebhookDeliveryEvent> events) {}
