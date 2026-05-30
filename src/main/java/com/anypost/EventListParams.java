package com.anypost;

import com.anypost.model.EventType;

import java.util.List;

/**
 * Filters for listing events. The window defaults to the last 24 hours and is
 * clamped to the plan's retention. All filters are exact-match except tags
 * (matches an event carrying any of the given tags).
 */
public final class EventListParams {

    public static final EventListParams DEFAULT = builder().build();

    private final Integer limit;
    private final String after;
    private final String start;
    private final String end;
    private final EventType eventType;
    private final String recipient;
    private final String emailId;
    private final String messageId;
    private final String domain;
    private final String topic;
    private final String campaign;
    private final String templateId;
    private final List<String> tags;

    private EventListParams(Builder b) {
        this.limit = b.limit;
        this.after = b.after;
        this.start = b.start;
        this.end = b.end;
        this.eventType = b.eventType;
        this.recipient = b.recipient;
        this.emailId = b.emailId;
        this.messageId = b.messageId;
        this.domain = b.domain;
        this.topic = b.topic;
        this.campaign = b.campaign;
        this.templateId = b.templateId;
        this.tags = b.tags;
    }

    public static Builder builder() {
        return new Builder();
    }

    void applyTo(Query query) {
        query.set("limit", limit);
        query.set("after", after);
        query.set("start", start);
        query.set("end", end);
        query.set("event_type", eventType == null ? null : eventType.value());
        query.set("recipient", recipient);
        query.set("email_id", emailId);
        query.set("message_id", messageId);
        query.set("domain", domain);
        query.set("topic", topic);
        query.set("campaign", campaign);
        query.set("template_id", templateId);
        if (tags != null && !tags.isEmpty()) {
            query.set("tags", String.join(",", tags));
        }
    }

    EventListParams withAfter(String cursor) {
        Builder b = toBuilder();
        b.after = cursor;
        return new EventListParams(b);
    }

    private Builder toBuilder() {
        Builder b = new Builder();
        b.limit = limit;
        b.after = after;
        b.start = start;
        b.end = end;
        b.eventType = eventType;
        b.recipient = recipient;
        b.emailId = emailId;
        b.messageId = messageId;
        b.domain = domain;
        b.topic = topic;
        b.campaign = campaign;
        b.templateId = templateId;
        b.tags = tags;
        return b;
    }

    /** A mutable builder for {@link EventListParams}. */
    public static final class Builder {
        private Integer limit;
        private String after;
        private String start;
        private String end;
        private EventType eventType;
        private String recipient;
        private String emailId;
        private String messageId;
        private String domain;
        private String topic;
        private String campaign;
        private String templateId;
        private List<String> tags;

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder after(String after) {
            this.after = after;
            return this;
        }

        /** ISO 8601 start of the window (inclusive). */
        public Builder start(String start) {
            this.start = start;
            return this;
        }

        /** ISO 8601 end of the window (exclusive). */
        public Builder end(String end) {
            this.end = end;
            return this;
        }

        public Builder eventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder recipient(String recipient) {
            this.recipient = recipient;
            return this;
        }

        /** Restrict to one message's {@code email_<uuidv7>} id. */
        public Builder emailId(String emailId) {
            this.emailId = emailId;
            return this;
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        /** A sending-domain hostname (not the {@code domain_<uuid>} id). */
        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder campaign(String campaign) {
            this.campaign = campaign;
            return this;
        }

        public Builder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        /** Restrict to events carrying any of these tags. Up to 10. */
        public Builder tags(String... tags) {
            this.tags = List.of(tags);
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public EventListParams build() {
            return new EventListParams(this);
        }
    }
}
