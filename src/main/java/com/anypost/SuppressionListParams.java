package com.anypost;

import com.anypost.model.SuppressionOrigin;
import com.anypost.model.SuppressionReason;

/** Filters for listing suppressions. {@link #DEFAULT} lists all, newest-first. */
public final class SuppressionListParams {

    public static final SuppressionListParams DEFAULT = builder().build();

    private final Integer limit;
    private final String after;
    private final String emailContains;
    private final String topic;
    private final SuppressionReason reason;
    private final SuppressionOrigin origin;

    private SuppressionListParams(Integer limit, String after, String emailContains,
                                  String topic, SuppressionReason reason, SuppressionOrigin origin) {
        this.limit = limit;
        this.after = after;
        this.emailContains = emailContains;
        this.topic = topic;
        this.reason = reason;
        this.origin = origin;
    }

    public static Builder builder() {
        return new Builder();
    }

    void applyTo(Query query) {
        query.set("limit", limit);
        query.set("after", after);
        query.set("email_contains", emailContains);
        query.set("topic", topic);
        query.set("reason", reason == null ? null : reason.value());
        query.set("origin", origin == null ? null : origin.value());
    }

    SuppressionListParams withAfter(String cursor) {
        return new SuppressionListParams(limit, cursor, emailContains, topic, reason, origin);
    }

    /** A mutable builder for {@link SuppressionListParams}. */
    public static final class Builder {
        private Integer limit;
        private String after;
        private String emailContains;
        private String topic;
        private SuppressionReason reason;
        private SuppressionOrigin origin;

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder after(String after) {
            this.after = after;
            return this;
        }

        /** Restrict to addresses containing this case-insensitive substring. */
        public Builder emailContains(String emailContains) {
            this.emailContains = emailContains;
            return this;
        }

        /** Restrict to one topic. Pass {@code "*"} for global entries only. */
        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder reason(SuppressionReason reason) {
            this.reason = reason;
            return this;
        }

        public Builder origin(SuppressionOrigin origin) {
            this.origin = origin;
            return this;
        }

        public SuppressionListParams build() {
            return new SuppressionListParams(limit, after, emailContains, topic, reason, origin);
        }
    }
}
