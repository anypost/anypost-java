package com.anypost.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** An event type a webhook can subscribe to. */
public enum WebhookEventType {
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

    WebhookEventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static WebhookEventType from(String value) {
        for (WebhookEventType t : values()) {
            if (t.value.equals(value)) {
                return t;
            }
        }
        return null;
    }
}
