package com.anypost.model;

/** The body for adding a manual suppression. */
public record SuppressionCreateParams(String email, String topic, String note) {

    public static Builder builder() {
        return new Builder();
    }

    /** A mutable builder for {@link SuppressionCreateParams}. */
    public static final class Builder {
        private String email;
        private String topic;
        private String note;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        /** Scope the suppression to one topic. Omit or {@code "*"} to block every topic. */
        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public SuppressionCreateParams build() {
            return new SuppressionCreateParams(email, topic, note);
        }
    }
}
