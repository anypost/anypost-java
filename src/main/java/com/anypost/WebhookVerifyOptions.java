package com.anypost;

import java.time.Duration;

/** Options for webhook signature verification. */
public final class WebhookVerifyOptions {

    private final Duration tolerance;
    private final Long now;

    private WebhookVerifyOptions(Duration tolerance, Long now) {
        this.tolerance = tolerance;
        this.now = now;
    }

    public static Builder builder() {
        return new Builder();
    }

    Duration tolerance() {
        return tolerance;
    }

    Long now() {
        return now;
    }

    /** A mutable builder for {@link WebhookVerifyOptions}. */
    public static final class Builder {
        private Duration tolerance = WebhookVerifier.DEFAULT_TOLERANCE;
        private Long now;

        /** The maximum delivery age. A zero or negative duration disables the freshness check. */
        public Builder tolerance(Duration tolerance) {
            this.tolerance = tolerance;
            return this;
        }

        /** Overrides the current time (Unix seconds) used for the freshness check. For tests. */
        public Builder now(long unixSeconds) {
            this.now = unixSeconds;
            return this;
        }

        public WebhookVerifyOptions build() {
            return new WebhookVerifyOptions(tolerance, now);
        }
    }
}
