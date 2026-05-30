package com.anypost;

/**
 * The stable, machine-readable classification of an API error. Branch on this
 * rather than on the HTTP status: the type is part of the API contract, the
 * status is not.
 */
public enum ErrorType {
    VALIDATION("validation_error"),
    AUTHENTICATION("authentication_error"),
    PERMISSION("permission_error"),
    NOT_FOUND("not_found"),
    CONFLICT("conflict"),
    IDEMPOTENCY_CONFLICT("idempotency_concurrent"),
    IDEMPOTENCY_MISMATCH("idempotency_mismatch"),
    WEBHOOK_ROTATION("webhook_rotation_in_progress"),
    RATE_LIMIT("rate_limit_exceeded"),
    PAYLOAD_TOO_LARGE("payload_too_large"),
    PROVISIONING("provisioning_error"),
    INTERNAL("internal_error"),
    /** A catch-all for an unrecognized server error type. */
    API_ERROR("api_error"),
    /**
     * No HTTP response was received (a network failure, timeout, or interruption).
     * The status is then 0 and the underlying cause is available via
     * {@link Throwable#getCause()}.
     */
    CONNECTION("connection_error");

    private final String value;

    ErrorType(String value) {
        this.value = value;
    }

    /** The wire string for this type (the API's {@code error.type}). */
    public String value() {
        return value;
    }

    /** Maps a wire string to a constant, falling back to {@link #API_ERROR} when unknown. */
    static ErrorType from(String value) {
        for (ErrorType t : values()) {
            if (t.value.equals(value)) {
                return t;
            }
        }
        return API_ERROR;
    }
}
