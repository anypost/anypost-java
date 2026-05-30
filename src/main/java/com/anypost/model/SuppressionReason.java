package com.anypost.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Why an address is suppressed. */
public enum SuppressionReason {
    PERMANENT_BOUNCE("permanent_bounce"),
    COMPLAINT("complaint"),
    UNSUBSCRIBED("unsubscribed"),
    MANUAL("manual");

    private final String value;

    SuppressionReason(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static SuppressionReason from(String value) {
        for (SuppressionReason r : values()) {
            if (r.value.equals(value)) {
                return r;
            }
        }
        return null;
    }
}
