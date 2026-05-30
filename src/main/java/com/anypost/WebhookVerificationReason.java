package com.anypost;

/**
 * The machine-readable cause of a webhook signature verification failure. Branch
 * on this rather than the message.
 */
public enum WebhookVerificationReason {
    /** The {@code Anypost-Signature} header could not be parsed. */
    MALFORMED_HEADER,
    /** The header carried no {@code t=} component. */
    NO_TIMESTAMP,
    /** The header carried no {@code v1=} component. */
    NO_SIGNATURES,
    /** The delivery is older than the tolerance. */
    TIMESTAMP_OUT_OF_TOLERANCE,
    /** No {@code v1=} component matched the computed signature. */
    NO_MATCH
}
