package com.anypost.model;

/**
 * A batch entry whose email is not a valid address.
 *
 * @param index the entry's 0-based position in the request
 * @param email the address as sent, or {@code null}
 */
public record SuppressionBatchInvalid(int index, String email) {}
