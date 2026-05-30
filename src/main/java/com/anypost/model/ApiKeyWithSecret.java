package com.anypost.model;

import java.util.List;

/**
 * A newly created key, including its plaintext secret. The secret is returned
 * only once, at creation.
 *
 * @param id             the {@code key_}-prefixed id
 * @param name           the human-readable name
 * @param keyPrefix      the first 12 characters of the secret
 * @param permissions    the permission level
 * @param allowedDomains the domains this key may send from, or {@code null} for all verified
 * @param allowedIps     the IPs/CIDRs allowed to use this key, or {@code null} for all
 * @param lastUsedAt     when the key was last used, or {@code null} if never
 * @param createdAt      the creation timestamp
 * @param key            the full API key &mdash; store it securely; it cannot be retrieved later
 */
public record ApiKeyWithSecret(
        String id,
        String name,
        String keyPrefix,
        Permissions permissions,
        List<String> allowedDomains,
        List<String> allowedIps,
        String lastUsedAt,
        String createdAt,
        String key) {}
