package com.anypost.model;

import java.util.ArrayList;
import java.util.List;

/** The body for creating a webhook. */
public record WebhookCreateParams(String name, String url, List<WebhookEventType> events) {

    public static Builder builder() {
        return new Builder();
    }

    /** A mutable builder for {@link WebhookCreateParams}. */
    public static final class Builder {
        private String name;
        private String url;
        private List<WebhookEventType> events;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /** An {@code https://} endpoint to receive signed deliveries. */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /** At least one event type to subscribe to. */
        public Builder events(WebhookEventType... events) {
            this.events = new ArrayList<>(List.of(events));
            return this;
        }

        public Builder events(List<WebhookEventType> events) {
            this.events = events;
            return this;
        }

        public WebhookCreateParams build() {
            return new WebhookCreateParams(name, url, events);
        }
    }
}
