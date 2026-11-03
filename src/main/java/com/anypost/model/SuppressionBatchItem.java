package com.anypost.model;

/**
 * One entry in a suppressions batch.
 *
 * @param email  the address to suppress
 * @param topic  the topic to scope to, or {@code null} for {@code "*"} (every topic)
 * @param reason a free-form reason; the server maps common values and aliases
 *               (e.g. {@code "hard bounce"}, {@code "spam report"}, {@code "opt-out"})
 *               and records anything else as {@code manual}, or {@code null}
 * @param note   a free-form note (max 2000 characters), or {@code null}
 */
public record SuppressionBatchItem(String email, String topic, String reason, String note) {

    public static Builder builder() {
        return new Builder();
    }

    /** A mutable builder for {@link SuppressionBatchItem}. */
    public static final class Builder {
        private String email;
        private String topic;
        private String reason;
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

        /** Why the address is suppressed. Aliases are accepted; unknown values record {@code manual}. */
        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public SuppressionBatchItem build() {
            return new SuppressionBatchItem(email, topic, reason, note);
        }
    }
}
