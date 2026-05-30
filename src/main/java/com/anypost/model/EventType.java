package com.anypost.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A customer-facing event type in the event stream. The same set is emitted via
 * webhooks; operational events are never returned here.
 */
public enum EventType {
    SENT("email.sent"),
    DELIVERED("email.delivered"),
    DELAYED("email.delayed"),
    BOUNCED("email.bounced"),
    COMPLAINED("email.complained"),
    SUPPRESSED("email.suppressed"),
    UNSUBSCRIBED("email.unsubscribed"),
    OPENED("email.opened"),
    CLICKED("email.clicked");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static EventType from(String value) {
        for (EventType t : values()) {
            if (t.value.equals(value)) {
                return t;
            }
        }
        return null;
    }
}
