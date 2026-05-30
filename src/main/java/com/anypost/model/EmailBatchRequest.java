package com.anypost.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The body for a batch send: 1&ndash;100 messages, with optional batch-wide
 * defaults.
 *
 * @param defaults fills any field an entry omits ({@code to} is always per-entry),
 *                 or {@code null}
 * @param emails   the 1&ndash;100 messages in the batch
 */
public record EmailBatchRequest(SendEmailRequest defaults, List<SendEmailRequest> emails) {

    public static Builder builder() {
        return new Builder();
    }

    /** A mutable builder for {@link EmailBatchRequest}. */
    public static final class Builder {
        private SendEmailRequest defaults;
        private List<SendEmailRequest> emails;

        /** Field defaults applied to every entry that does not set its own. */
        public Builder defaults(SendEmailRequest defaults) {
            this.defaults = defaults;
            return this;
        }

        public Builder emails(List<SendEmailRequest> emails) {
            this.emails = emails;
            return this;
        }

        /** Append a single message to the batch. */
        public Builder email(SendEmailRequest email) {
            if (this.emails == null) {
                this.emails = new ArrayList<>();
            }
            this.emails.add(email);
            return this;
        }

        public EmailBatchRequest build() {
            return new EmailBatchRequest(defaults, emails);
        }
    }
}
