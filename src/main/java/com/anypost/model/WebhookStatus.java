package com.anypost.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A webhook's delivery state. Only {@link #ACTIVE} and {@link #DISABLED} can be
 * set through the API; {@link #CIRCUIT_DISABLED} is server-managed.
 */
public enum WebhookStatus {
    ACTIVE("active"),
    DISABLED("disabled"),
    CIRCUIT_DISABLED("circuit_disabled");

    private final String value;

    WebhookStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static WebhookStatus from(String value) {
        for (WebhookStatus s : values()) {
            if (s.value.equals(value)) {
                return s;
            }
        }
        return null;
    }
}
