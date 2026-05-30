package com.anypost.model;

/**
 * One attachment on a message.
 *
 * <p>{@code content} is the raw file bytes (for example, the result of
 * {@code Files.readAllBytes}). The SDK base64-encodes it on the wire via
 * Jackson's default byte-array handling &mdash; do not pre-encode it.
 *
 * @param filename    the file name shown to the recipient
 * @param content     the raw file bytes; encoded to base64 on the wire
 * @param contentType the MIME type, or {@code null} to let the server default to
 *                    {@code application/octet-stream}
 * @param contentId   marks the attachment inline (referenced from the HTML via
 *                    {@code cid:}), or {@code null} for a regular attachment
 */
public record Attachment(String filename, byte[] content, String contentType, String contentId) {

    /** An attachment with the server-default content type and no inline content id. */
    public static Attachment of(String filename, byte[] content) {
        return new Attachment(filename, content, null, null);
    }

    /** An attachment with an explicit content type and no inline content id. */
    public static Attachment of(String filename, byte[] content, String contentType) {
        return new Attachment(filename, content, contentType, null);
    }

    /** An inline attachment, referenced from the HTML body via {@code cid:<contentId>}. */
    public static Attachment inline(String filename, byte[] content, String contentType, String contentId) {
        return new Attachment(filename, content, contentType, contentId);
    }
}
