package com.anypost.model;

/**
 * The body for updating a domain. Only tracking configuration is mutable; the
 * domain name is immutable.
 *
 * @param tracking the tracking configuration changes
 */
public record DomainUpdateParams(DomainTrackingParams tracking) {

    public static DomainUpdateParams of(DomainTrackingParams tracking) {
        return new DomainUpdateParams(tracking);
    }
}
