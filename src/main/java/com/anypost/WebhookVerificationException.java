package com.anypost;

/**
 * Thrown when a webhook delivery's signature cannot be verified. Extends
 * {@link RuntimeException}; branch on {@link #reason()} in your handler.
 */
public final class WebhookVerificationException extends RuntimeException {

    private final WebhookVerificationReason reason;

    WebhookVerificationException(WebhookVerificationReason reason, String message) {
        super(message);
        this.reason = reason;
    }

    /** The machine-readable cause. Branch on this. */
    public WebhookVerificationReason reason() {
        return reason;
    }
}
