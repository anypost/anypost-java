package com.anypost.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** The provenance of a suppression row. */
public enum SuppressionOrigin {
    AUTO("auto"),
    MANUAL("manual");

    private final String value;

    SuppressionOrigin(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static SuppressionOrigin from(String value) {
        for (SuppressionOrigin o : values()) {
            if (o.value.equals(value)) {
                return o;
            }
        }
        return null;
    }
}
