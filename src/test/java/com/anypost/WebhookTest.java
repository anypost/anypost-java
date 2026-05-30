package com.anypost;

import com.anypost.model.WebhookDelivery;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WebhookTest {

    private static final String SECRET = "whsec_test_secret";
    private static final long TS = 1_700_000_000L;
    private static final byte[] PAYLOAD =
            ("{\"batch_id\":\"batch_1\",\"timestamp\":" + TS + ",\"events\":["
                    + "{\"id\":\"evt_1\",\"type\":\"email.delivered\",\"occurred_at\":\"t\",\"data\":{\"email_id\":\"email_1\"}}]}")
                    .getBytes(StandardCharsets.UTF_8);

    private static String sign(String secret, long timestamp, byte[] payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        mac.update((timestamp + ".").getBytes(StandardCharsets.US_ASCII));
        mac.update(payload);
        StringBuilder hex = new StringBuilder();
        for (byte b : mac.doFinal()) {
            hex.append(Character.forDigit((b >> 4) & 0xf, 16)).append(Character.forDigit(b & 0xf, 16));
        }
        return hex.toString();
    }

    private static WebhookVerifyOptions atSigningTime() {
        return WebhookVerifyOptions.builder().now(TS).build();
    }

    @Test
    void verifiesAndUnwrapsAValidDelivery() throws Exception {
        String header = "t=" + TS + ",v1=" + sign(SECRET, TS, PAYLOAD);
        WebhookDelivery delivery = WebhookVerifier.unwrap(PAYLOAD, header, SECRET, atSigningTime());
        assertEquals("batch_1", delivery.batchId());
        assertEquals(TS, delivery.timestamp());
        assertEquals("evt_1", delivery.events().get(0).id());
        assertEquals("email_1", delivery.events().get(0).data().get("email_id"));
    }

    @Test
    void rejectsWrongSecret() throws Exception {
        String header = "t=" + TS + ",v1=" + sign("whsec_other", TS, PAYLOAD);
        WebhookVerificationException e = assertThrows(WebhookVerificationException.class,
                () -> WebhookVerifier.verifySignature(PAYLOAD, header, SECRET, atSigningTime()));
        assertEquals(WebhookVerificationReason.NO_MATCH, e.reason());
    }

    @Test
    void acceptsAnyMatchingSignatureDuringRotation() throws Exception {
        // One bogus v1 and one valid v1 (a secret rotation in progress) still passes.
        String header = "t=" + TS + ",v1=deadbeef,v1=" + sign(SECRET, TS, PAYLOAD);
        WebhookVerifier.verifySignature(PAYLOAD, header, SECRET, atSigningTime());
    }

    @Test
    void rejectsStaleDelivery() throws Exception {
        String header = "t=" + TS + ",v1=" + sign(SECRET, TS, PAYLOAD);
        WebhookVerifyOptions options = WebhookVerifyOptions.builder().now(TS + 1000).build();
        WebhookVerificationException e = assertThrows(WebhookVerificationException.class,
                () -> WebhookVerifier.verifySignature(PAYLOAD, header, SECRET, options));
        assertEquals(WebhookVerificationReason.TIMESTAMP_OUT_OF_TOLERANCE, e.reason());
    }

    @Test
    void toleranceZeroDisablesFreshnessCheck() throws Exception {
        String header = "t=" + TS + ",v1=" + sign(SECRET, TS, PAYLOAD);
        WebhookVerifyOptions options =
                WebhookVerifyOptions.builder().tolerance(Duration.ZERO).now(TS + 1_000_000).build();
        WebhookVerifier.verifySignature(PAYLOAD, header, SECRET, options); // no throw
    }

    @Test
    void rejectsMalformedHeaders() {
        assertEquals(WebhookVerificationReason.MALFORMED_HEADER,
                assertThrows(WebhookVerificationException.class,
                        () -> WebhookVerifier.verifySignature(PAYLOAD, "", SECRET)).reason());
        assertEquals(WebhookVerificationReason.NO_TIMESTAMP,
                assertThrows(WebhookVerificationException.class,
                        () -> WebhookVerifier.verifySignature(PAYLOAD, "v1=abc", SECRET)).reason());
        assertEquals(WebhookVerificationReason.NO_SIGNATURES,
                assertThrows(WebhookVerificationException.class,
                        () -> WebhookVerifier.verifySignature(PAYLOAD, "t=" + TS, SECRET, atSigningTime())).reason());
    }
}
