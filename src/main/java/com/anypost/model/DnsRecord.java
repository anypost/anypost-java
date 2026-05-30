package com.anypost.model;

/**
 * A DNS record the customer must publish to verify a domain or its branded
 * tracking.
 *
 * @param type    the record type ({@code CNAME} is the only value today)
 * @param name    the record name to publish, relative to the registered apex
 * @param value   the CNAME target (absolute FQDN)
 * @param purpose one of {@code verification}, {@code dkim}, or {@code tracking}
 */
public record DnsRecord(String type, String name, String value, String purpose) {}
