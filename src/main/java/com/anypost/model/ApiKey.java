package com.anypost.model;

import java.util.List;

/**
 * An API key's metadata. The plaintext secret is never returned here.
 *
 * @param id             the {@code key_}-prefixed id
 * @param name           the human-readable name
 * @param keyPrefix      the first 12 characters of the secret, shown for identification
 * @param permissions    the permission level
 * @param allowedDomains the domains this key may send from, or {@code null} for all verified
 * @param allowedIps     the IPs/CIDRs allowed to use this key, or {@code null} for all
 * @param lastUsedAt     when the key was last used, or {@code null} if never
 * @param createdAt      the creation timestamp
 */
public record ApiKey(
        String id,
        String name,
        String keyPrefix,
        Permissions permissions,
        List<String> allowedDomains,
        List<String> allowedIps,
        String lastUsedAt,
        String createdAt) {}
