package com.anypost.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** The permission level of an API key. */
public enum Permissions {
    /** Management and send access. */
    FULL("full"),
    /** Send access only. */
    SEND_ONLY("send_only");

    private final String value;

    Permissions(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static Permissions from(String value) {
        for (Permissions p : values()) {
            if (p.value.equals(value)) {
                return p;
            }
        }
        return null;
    }
}
