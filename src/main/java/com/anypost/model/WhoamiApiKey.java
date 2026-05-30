package com.anypost.model;

/**
 * Identifies the API key on the request.
 *
 * @param id          the {@code key_}-prefixed id
 * @param permissions the key's permission level
 */
public record WhoamiApiKey(String id, Permissions permissions) {}
