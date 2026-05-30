package com.anypost.model;

import java.util.ArrayList;
import java.util.List;

/** The body for updating a webhook. It does not rotate the signing secret. */
public record WebhookUpdateParams(
        String name,
        String url,
        List<WebhookEventType> events,
        WebhookStatus status) {

    public static Builder builder() {
        return new Builder();
    }

    /** A mutable builder for {@link WebhookUpdateParams}. */
    public static final class Builder {
        private String name;
        private String url;
        private List<WebhookEventType> events;
        private WebhookStatus status;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder events(WebhookEventType... events) {
            this.events = new ArrayList<>(List.of(events));
            return this;
        }

        public Builder events(List<WebhookEventType> events) {
            this.events = events;
            return this;
        }

        /** Set {@link WebhookStatus#DISABLED} to pause delivery or {@link WebhookStatus#ACTIVE} to resume. */
        public Builder status(WebhookStatus status) {
            this.status = status;
            return this;
        }

        public WebhookUpdateParams build() {
            return new WebhookUpdateParams(name, url, events, status);
        }
    }
}
