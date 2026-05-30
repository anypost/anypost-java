package com.anypost;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-call overrides: an idempotency key and/or extra request headers. Pass one
 * as the last argument to any resource method, or {@code null} for defaults.
 */
public final class RequestOptions {

    private final String idempotencyKey;
    private final Map<String, String> headers;

    private RequestOptions(String idempotencyKey, Map<String, String> headers) {
        this.idempotencyKey = idempotencyKey;
        this.headers = headers;
    }

    /**
     * Sets the {@code Idempotency-Key} for a send. Reusing a key with an identical
     * body replays the stored result; reusing it with a different body fails with
     * {@link ErrorType#IDEMPOTENCY_MISMATCH}. Only the send endpoints honor it.
     */
    public static RequestOptions idempotencyKey(String key) {
        return builder().idempotencyKey(key).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    String idempotencyKeyValue() {
        return idempotencyKey;
    }

    Map<String, String> headerMap() {
        return headers == null ? Collections.emptyMap() : headers;
    }

    /** A mutable builder for {@link RequestOptions}. */
    public static final class Builder {
        private String idempotencyKey;
        private Map<String, String> headers;

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        /** Add (or override) a single header on this request. */
        public Builder header(String name, String value) {
            if (this.headers == null) {
                this.headers = new LinkedHashMap<>();
            }
            this.headers.put(name, value);
            return this;
        }

        public RequestOptions build() {
            return new RequestOptions(idempotencyKey, headers);
        }
    }
}
