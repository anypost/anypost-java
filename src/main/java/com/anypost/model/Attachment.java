package com.anypost.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Base64;

/**
 * One attachment on a message.
 *
 * <p>Set the content exactly one of two ways. {@code content} takes the raw file
 * bytes (for example, the result of {@code Files.readAllBytes}) and the SDK
 * base64-encodes them on the wire. {@code contentBase64} takes content that is
 * already base64 at rest and sends it through verbatim, so nothing is decoded
 * just to be re-encoded. Setting both, or neither, throws.
 *
 * @param filename      the file name shown to the recipient
 * @param content       the raw file bytes, or {@code null} when using {@code contentBase64}
 * @param contentBase64 already-base64-encoded content, or {@code null} when using {@code content}
 * @param contentType   the MIME type, or {@code null} to let the server default to
 *                      {@code application/octet-stream}
 * @param contentId     marks the attachment inline (referenced from the HTML via
 *                      {@code cid:}), or {@code null} for a regular attachment
 */
public record Attachment(
        String filename,
        byte[] content,
        String contentBase64,
        String contentType,
        String contentId) {

    public Attachment {
        if (content != null && contentBase64 != null) {
            throw new IllegalArgumentException(
                    "Attachment '" + filename + "' sets both content and contentBase64; set exactly one.");
        }
        if (content == null && contentBase64 == null) {
            throw new IllegalArgumentException(
                    "Attachment '" + filename + "' has no content; set content (raw bytes) or contentBase64.");
        }
    }

    /**
     * The pre-{@code contentBase64} canonical shape, kept so existing callers of the
     * four-argument constructor keep compiling.
     */
    public Attachment(String filename, byte[] content, String contentType, String contentId) {
        this(filename, content, null, contentType, contentId);
    }

    /** An attachment with the server-default content type and no inline content id. */
    public static Attachment of(String filename, byte[] content) {
        return new Attachment(filename, content, null, null, null);
    }

    /** An attachment with an explicit content type and no inline content id. */
    public static Attachment of(String filename, byte[] content, String contentType) {
        return new Attachment(filename, content, null, contentType, null);
    }

    /** An attachment built from already-base64-encoded content. */
    public static Attachment ofBase64(String filename, String contentBase64) {
        return new Attachment(filename, null, contentBase64, null, null);
    }

    /** An attachment built from already-base64-encoded content, with an explicit content type. */
    public static Attachment ofBase64(String filename, String contentBase64, String contentType) {
        return new Attachment(filename, null, contentBase64, contentType, null);
    }

    /** An inline attachment, referenced from the HTML body via {@code cid:<contentId>}. */
    public static Attachment inline(String filename, byte[] content, String contentType, String contentId) {
        return new Attachment(filename, content, null, contentType, contentId);
    }

    /**
     * An inline attachment built from already-base64-encoded content, referenced from
     * the HTML body via {@code cid:<contentId>}.
     */
    public static Attachment inlineBase64(
            String filename, String contentBase64, String contentType, String contentId) {
        return new Attachment(filename, null, contentBase64, contentType, contentId);
    }

    @JsonIgnore
    public byte[] content() {
        return content;
    }

    @JsonIgnore
    public String contentBase64() {
        return contentBase64;
    }

    /**
     * The base64 string that goes on the wire. The two content components collapse
     * into this single {@code content} field; the constructor has already guaranteed
     * exactly one of them is set.
     */
    @JsonProperty("content")
    String wireContent() {
        return contentBase64 != null ? contentBase64 : Base64.getEncoder().encodeToString(content);
    }
}
