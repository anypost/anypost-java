package com.anypost.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** The one-click unsubscribe behavior for a send. */
public enum UnsubscribeMode {
    /**
     * Mint a per-recipient signed token and inject RFC 8058 unsubscribe headers.
     * Requires a topic on the send.
     */
    GENERATE("generate"),
    /** Inject nothing &mdash; for transactional sends that carry no unsubscribe semantics. */
    NONE("none");

    private final String value;

    UnsubscribeMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static UnsubscribeMode from(String value) {
        for (UnsubscribeMode m : values()) {
            if (m.value.equals(value)) {
                return m;
            }
        }
        return null;
    }
}
