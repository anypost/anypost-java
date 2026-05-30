package com.anypost;

import com.anypost.model.WebhookDelivery;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the signature on an Anypost webhook delivery.
 *
 * <p>These are static methods &mdash; they need the signing secret, not an API
 * key, so call them in your handler without a client. Pass the <b>raw</b> request
 * body (the exact bytes received, before JSON parsing), the
 * {@code Anypost-Signature} header value, and the webhook's signing secret.
 *
 * <p>The header may carry more than one {@code v1=} component during a secret
 * rotation; a match on any one passes, so deliveries keep verifying across a
 * rotation.
 */
public final class WebhookVerifier {

    /**
     * The default maximum age of a webhook delivery, measured from its signed
     * timestamp. Deliveries older than this are rejected to bound replay.
     */
    public static final Duration DEFAULT_TOLERANCE = Duration.ofSeconds(300);

    private WebhookVerifier() {}

    /** Verifies a delivery's signature with the default 5-minute tolerance. */
    public static void verifySignature(byte[] payload, String signatureHeader, String secret) {
        verifySignature(payload, signatureHeader, secret, null);
    }

    /**
     * Verifies a delivery's signature. Throws {@link WebhookVerificationException}
     * on any failure; returns normally on success.
     */
    public static void verifySignature(byte[] payload, String signatureHeader, String secret,
                                       WebhookVerifyOptions options) {
        Duration tolerance = options != null ? options.tolerance() : DEFAULT_TOLERANCE;
        Long nowOverride = options != null ? options.now() : null;

        Parsed parsed = parseSignatureHeader(signatureHeader);

        if (tolerance != null && !tolerance.isZero() && !tolerance.isNegative()) {
            long now = nowOverride != null ? nowOverride : Instant.now().getEpochSecond();
            if (now - parsed.timestamp > tolerance.getSeconds()) {
                throw new WebhookVerificationException(
                        WebhookVerificationReason.TIMESTAMP_OUT_OF_TOLERANCE,
                        "Timestamp " + parsed.timestamp + " is older than the "
                                + tolerance.getSeconds() + "s tolerance.");
            }
        }

        byte[] expected = hmacHex(secret, parsed.timestamp, payload).getBytes(StandardCharsets.UTF_8);

        // Constant-time over every candidate: accumulate without early exit.
        boolean matched = false;
        for (String candidate : parsed.signatures) {
            if (MessageDigest.isEqual(expected, candidate.getBytes(StandardCharsets.UTF_8))) {
                matched = true;
            }
        }
        if (!matched) {
            throw new WebhookVerificationException(WebhookVerificationReason.NO_MATCH,
                    "No signature in the header matched the computed signature.");
        }
    }

    /** Verifies a delivery and returns its parsed body, with the default tolerance. */
    public static WebhookDelivery unwrap(byte[] payload, String signatureHeader, String secret) {
        return unwrap(payload, signatureHeader, secret, null);
    }

    /**
     * Verifies a delivery and returns its parsed body. A thin wrapper over
     * {@link #verifySignature} that deserializes only after the signature checks out.
     */
    public static WebhookDelivery unwrap(byte[] payload, String signatureHeader, String secret,
                                         WebhookVerifyOptions options) {
        verifySignature(payload, signatureHeader, secret, options);
        try {
            return Json.MAPPER.readValue(payload, WebhookDelivery.class);
        } catch (Exception e) {
            throw new WebhookVerificationException(WebhookVerificationReason.MALFORMED_HEADER,
                    "decoding webhook payload: " + e.getMessage());
        }
    }

    private static String hmacHex(String secret, long timestamp, byte[] payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update((timestamp + ".").getBytes(StandardCharsets.US_ASCII));
            mac.update(payload);
            return toHex(mac.doFinal());
        } catch (GeneralSecurityException e) {
            // HmacSHA256 is always available and the key is never invalid.
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xf, 16));
            sb.append(Character.forDigit(b & 0xf, 16));
        }
        return sb.toString();
    }

    private static Parsed parseSignatureHeader(String header) {
        if (header == null || header.isEmpty()) {
            throw new WebhookVerificationException(WebhookVerificationReason.MALFORMED_HEADER,
                    "The Anypost-Signature header is empty.");
        }

        Long timestamp = null;
        List<String> signatures = new ArrayList<>();
        for (String part : header.split(",")) {
            int eq = part.indexOf('=');
            if (eq < 0) {
                continue;
            }
            String key = part.substring(0, eq).trim();
            String value = part.substring(eq + 1).trim();
            if (key.equals("t")) {
                try {
                    timestamp = Long.parseLong(value);
                } catch (NumberFormatException ignored) {
                    // leave timestamp null; reported below
                }
            } else if (key.equals("v1")) {
                signatures.add(value);
            }
        }

        if (timestamp == null) {
            throw new WebhookVerificationException(WebhookVerificationReason.NO_TIMESTAMP,
                    "The Anypost-Signature header has no timestamp (t=).");
        }
        if (signatures.isEmpty()) {
            throw new WebhookVerificationException(WebhookVerificationReason.NO_SIGNATURES,
                    "The Anypost-Signature header has no v1= signature.");
        }
        return new Parsed(timestamp, signatures);
    }

    private record Parsed(long timestamp, List<String> signatures) {}
}
