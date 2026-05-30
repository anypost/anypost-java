package com.anypost.model;

/**
 * Overrides the sending domain's open/click tracking defaults for one message. A
 * {@code null} field leaves that dimension at the domain default.
 *
 * @param opens  inject the open-tracking pixel into the HTML body, or {@code null}
 *               to use the domain default
 * @param clicks rewrite links for click tracking, or {@code null} to use the
 *               domain default
 */
public record Tracking(Boolean opens, Boolean clicks) {

    /** Set both open and click tracking explicitly. */
    public static Tracking of(Boolean opens, Boolean clicks) {
        return new Tracking(opens, clicks);
    }

    /** Override only open tracking, leaving click tracking at the domain default. */
    public static Tracking opens(boolean opens) {
        return new Tracking(opens, null);
    }

    /** Override only click tracking, leaving open tracking at the domain default. */
    public static Tracking clicks(boolean clicks) {
        return new Tracking(null, clicks);
    }
}
