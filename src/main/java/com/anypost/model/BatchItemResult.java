package com.anypost.model;

/**
 * One entry's outcome in a batch send. Discriminate on {@code status}:
 * {@code "queued"} entries carry {@code id} and {@code createdAt}; {@code "failed"}
 * entries carry {@code error}.
 *
 * @param status    {@code "queued"} or {@code "failed"}
 * @param index     the zero-based position in the request's {@code emails} list
 * @param id        the queued message id, or {@code null} on failure
 * @param createdAt the acceptance timestamp, or {@code null} on failure
 * @param error     the failure detail, or {@code null} when queued
 */
public record BatchItemResult(
        String status,
        int index,
        String id,
        String createdAt,
        BatchItemError error) {

    /** Whether this entry was accepted. */
    public boolean isQueued() {
        return "queued".equals(status);
    }
}
