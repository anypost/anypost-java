package com.anypost.model;

/**
 * Returned by a successful single send.
 *
 * @param id        the public message identifier ({@code email_<uuidv7>})
 * @param createdAt the ISO 8601 acceptance timestamp
 */
public record SendResponse(String id, String createdAt) {}
