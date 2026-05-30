package com.anypost.model;

import java.util.List;

/**
 * A domain's branded open/click tracking configuration. Independent of mail-flow
 * verification.
 *
 * @param opensEnabled        whether open tracking is on
 * @param clicksEnabled       whether click tracking is on
 * @param subdomain           the tracking subdomain prefix, or {@code null} when off
 * @param dnsRecords          the branded-tracking records to publish (empty when off)
 * @param status              {@code disabled}, {@code pending}, or {@code verified}
 * @param verificationFailure the most recent tracking-CNAME failure, or {@code null}
 * @param verifiedAt          when the tracking CNAME was last observed resolving, or {@code null}
 */
public record DomainTracking(
        boolean opensEnabled,
        boolean clicksEnabled,
        String subdomain,
        List<DnsRecord> dnsRecords,
        String status,
        VerificationFailure verificationFailure,
        String verifiedAt) {}
