package com.anypost.model;

/**
 * The outcome of a synchronous test delivery. A bad endpoint never throws &mdash;
 * read {@code delivered} and {@code statusCode}.
 *
 * @param delivered           {@code true} only when the endpoint returned a 2xx status
 * @param statusCode          the HTTP status the endpoint returned, or {@code null} on a network failure
 * @param latencyMs           wall-clock time from request start to response or error
 * @param error               a human-readable failure reason, or {@code null} on success
 * @param responseBodyPreview a truncated preview of the endpoint's response body, or {@code null}
 */
public record WebhookTestResult(
        boolean delivered,
        Integer statusCode,
        int latencyMs,
        String error,
        String responseBodyPreview) {}
