package com.anypost.model;

/**
 * The mutable tracking configuration on a domain update. Leave a field
 * {@code null} to leave it unchanged.
 *
 * @param opensEnabled  turn open tracking on or off, or {@code null} to leave unchanged
 * @param clicksEnabled turn click tracking on or off, or {@code null} to leave unchanged
 * @param subdomain     the tracking subdomain prefix (required when turning either
 *                      flag on), or {@code null} to leave unchanged
 */
public record DomainTrackingParams(Boolean opensEnabled, Boolean clicksEnabled, String subdomain) {

    public static Builder builder() {
        return new Builder();
    }

    /** A mutable builder for {@link DomainTrackingParams}. */
    public static final class Builder {
        private Boolean opensEnabled;
        private Boolean clicksEnabled;
        private String subdomain;

        public Builder opensEnabled(boolean opensEnabled) {
            this.opensEnabled = opensEnabled;
            return this;
        }

        public Builder clicksEnabled(boolean clicksEnabled) {
            this.clicksEnabled = clicksEnabled;
            return this;
        }

        public Builder subdomain(String subdomain) {
            this.subdomain = subdomain;
            return this;
        }

        public DomainTrackingParams build() {
            return new DomainTrackingParams(opensEnabled, clicksEnabled, subdomain);
        }
    }
}
