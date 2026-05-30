package com.anypost.model;

/**
 * Configures one-click unsubscribe headers for a send.
 *
 * @param mode        the unsubscribe behavior
 * @param displayName the human-readable label rendered on the hosted confirmation
 *                    page, or {@code null}
 */
public record Unsubscribe(UnsubscribeMode mode, String displayName) {

    /** Generate a per-recipient one-click unsubscribe. Requires a topic on the send. */
    public static Unsubscribe generate() {
        return new Unsubscribe(UnsubscribeMode.GENERATE, null);
    }

    /** Generate a per-recipient one-click unsubscribe with a confirmation-page label. */
    public static Unsubscribe generate(String displayName) {
        return new Unsubscribe(UnsubscribeMode.GENERATE, displayName);
    }

    /** Inject no unsubscribe header. */
    public static Unsubscribe none() {
        return new Unsubscribe(UnsubscribeMode.NONE, null);
    }
}
