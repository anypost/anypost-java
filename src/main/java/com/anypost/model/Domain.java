package com.anypost.model;

import java.util.List;

/**
 * A sending domain and its mail-flow verification state.
 *
 * @param id                  the {@code domain_}-prefixed id
 * @param name                the domain name, e.g. {@code example.com}
 * @param status              {@code pending} until the mail-flow CNAMEs resolve, then {@code verified}
 * @param dnsRecords          the mail-flow records to publish
 * @param verificationFailure the most recent mail-flow failure, or {@code null}
 * @param tracking            the branded tracking configuration and its status
 * @param createdAt           the creation timestamp
 * @param verifiedAt          when the domain last transitioned to verified, or {@code null}
 */
public record Domain(
        String id,
        String name,
        String status,
        List<DnsRecord> dnsRecords,
        VerificationFailure verificationFailure,
        DomainTracking tracking,
        String createdAt,
        String verifiedAt) {}
