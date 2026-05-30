package com.anypost;

import com.anypost.model.Attachment;
import com.anypost.model.BatchResponse;
import com.anypost.model.EmailBatchRequest;
import com.anypost.model.SendEmailRequest;
import com.anypost.model.SendResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static com.anypost.TestSupport.MockTransport;
import static com.anypost.TestSupport.client;
import static com.anypost.TestSupport.header;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailTest {

    private static final String SEND_OK = "{\"id\":\"email_018f\",\"created_at\":\"2026-04-30T12:00:00Z\"}";

    @Test
    void sendSerializesBodyAndParsesResponse() throws Exception {
        MockTransport transport = new MockTransport().enqueue(202, SEND_OK);
        SendResponse sent = client(transport).email.send(SendEmailRequest.builder()
                .from("Acme <hello@example.com>")
                .to("alex@customer.com")
                .replyTo("support@example.com")
                .subject("Welcome")
                .html("<p>Hi</p>")
                .tags("welcome")
                .build());

        assertEquals("email_018f", sent.id());
        assertEquals("2026-04-30T12:00:00Z", sent.createdAt());

        var request = transport.lastRequest();
        assertEquals("POST", request.method());
        assertEquals("https://api.test/v1/email", request.uri().toString());
        assertEquals("application/json", header(request, "Content-Type"));

        JsonNode body = TestSupport.MAPPER.readTree(TestSupport.bodyOf(request));
        assertEquals("Acme <hello@example.com>", body.get("from").asText());
        assertEquals("alex@customer.com", body.get("to").get(0).asText());
        assertEquals("support@example.com", body.get("reply_to").get(0).asText());
        assertEquals("welcome", body.get("tags").get(0).asText());
    }

    @Test
    void attachmentContentIsBase64Encoded() throws Exception {
        MockTransport transport = new MockTransport().enqueue(202, SEND_OK);
        byte[] raw = {1, 2, 3, 4, 5};
        client(transport).email.send(SendEmailRequest.builder()
                .from("a@example.com").to("b@example.com").subject("S").text("T")
                .attachment(Attachment.of("f.bin", raw))
                .build());

        JsonNode body = TestSupport.MAPPER.readTree(TestSupport.bodyOf(transport.lastRequest()));
        String encoded = body.get("attachments").get(0).get("content").asText();
        assertArrayEquals(raw, Base64.getDecoder().decode(encoded));
    }

    @Test
    void noIdempotencyKeyWhenRetriesDisabled() {
        MockTransport transport = new MockTransport().enqueue(202, SEND_OK);
        client(transport, 0).email.send(simple());
        assertNull(header(transport.lastRequest(), "Idempotency-Key"));
    }

    @Test
    void autoIdempotencyKeyWhenRetriesEnabled() {
        MockTransport transport = new MockTransport().enqueue(202, SEND_OK);
        client(transport, 2).email.send(simple());
        String key = header(transport.lastRequest(), "Idempotency-Key");
        assertNotNull(key);
        assertFalse(key.isBlank());
    }

    @Test
    void explicitIdempotencyKeyIsPassedThrough() {
        MockTransport transport = new MockTransport().enqueue(202, SEND_OK);
        client(transport, 2).email.send(simple(), RequestOptions.idempotencyKey("order-4823"));
        assertEquals("order-4823", header(transport.lastRequest(), "Idempotency-Key"));
    }

    @Test
    void batchMixedOutcomeDoesNotThrow() throws Exception {
        String batch = "{\"summary\":{\"total\":2,\"queued\":1,\"failed\":1},"
                + "\"data\":[{\"status\":\"queued\",\"index\":0,\"id\":\"email_1\",\"created_at\":\"t\"},"
                + "{\"status\":\"failed\",\"index\":1,\"error\":{\"type\":\"validation_error\",\"message\":\"bad\"}}]}";
        MockTransport transport = new MockTransport().enqueue(207, batch);

        BatchResponse result = client(transport).email.sendBatch(EmailBatchRequest.builder()
                .defaults(SendEmailRequest.builder().from("a@example.com").build())
                .email(SendEmailRequest.builder().to("b@example.com").subject("A").text("a").build())
                .email(SendEmailRequest.builder().to("c@example.com").subject("B").text("b").build())
                .build());

        assertEquals(2, result.summary().total());
        assertEquals(1, result.summary().queued());
        assertEquals(1, result.summary().failed());
        assertTrue(result.data().get(0).isQueued());
        assertFalse(result.data().get(1).isQueued());
        assertEquals("validation_error", result.data().get(1).error().type());

        // Defaults are nested under the documented key.
        JsonNode body = TestSupport.MAPPER.readTree(TestSupport.bodyOf(transport.lastRequest()));
        assertEquals("a@example.com", body.get("defaults").get("from").asText());
        assertEquals(2, body.get("emails").size());
    }

    private static SendEmailRequest simple() {
        return SendEmailRequest.builder().from("a@example.com").to("b@example.com").subject("S").text("T").build();
    }

    private static void assertArrayEquals(byte[] expected, byte[] actual) {
        org.junit.jupiter.api.Assertions.assertArrayEquals(expected, actual);
    }
}
