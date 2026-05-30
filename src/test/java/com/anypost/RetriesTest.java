package com.anypost;

import com.anypost.model.SendEmailRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.anypost.TestSupport.MockTransport;
import static com.anypost.TestSupport.client;
import static com.anypost.TestSupport.header;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetriesTest {

    private static final String OK = "{\"id\":\"email_1\",\"created_at\":\"t\"}";
    private static final SendEmailRequest MSG =
            SendEmailRequest.builder().from("a@example.com").to("b@example.com").subject("S").text("T").build();

    @Test
    void retriesOn503ThenSucceeds() {
        MockTransport transport = new MockTransport().enqueue(503, "{}").enqueue(202, OK);
        var sent = client(transport, 2).email.send(MSG);
        assertEquals("email_1", sent.id());
        assertEquals(2, transport.count());
    }

    @Test
    void retriesOnNetworkErrorThenSucceeds() {
        MockTransport transport = new MockTransport()
                .enqueueError(new IOException("reset")).enqueue(202, OK);
        var sent = client(transport, 2).email.send(MSG);
        assertEquals("email_1", sent.id());
        assertEquals(2, transport.count());
    }

    @Test
    void doesNotRetryOn400() {
        MockTransport transport = new MockTransport()
                .enqueue(400, "{\"error\":{\"type\":\"validation_error\",\"message\":\"bad\"}}");
        assertThrows(AnypostException.class, () -> client(transport, 2).email.send(MSG));
        assertEquals(1, transport.count());
    }

    @Test
    void exhaustsRetriesThenThrows() {
        MockTransport transport = new MockTransport()
                .enqueue(503, "{}").enqueue(503, "{}").enqueue(503, "{}");
        AnypostException e = assertThrows(AnypostException.class, () -> client(transport, 2).email.send(MSG));
        assertEquals(503, e.status());
        assertEquals(3, transport.count()); // initial + 2 retries
    }

    @Test
    void honorsRetryAfterHeader() {
        MockTransport transport = new MockTransport()
                .enqueue(503, "{}", Map.of("Retry-After", List.of("2")))
                .enqueue(202, OK);
        Anypost anypost = client(transport, 2);
        List<Long> slept = new ArrayList<>();
        anypost.executor().sleeper = slept::add;

        anypost.email.send(MSG);
        assertEquals(List.of(2000L), slept); // 2s Retry-After honored
    }

    @Test
    void autoIdempotencyKeyIsReusedAcrossRetries() {
        MockTransport transport = new MockTransport().enqueue(503, "{}").enqueue(202, OK);
        client(transport, 2).email.send(MSG);

        String first = header(transport.requests.get(0), "Idempotency-Key");
        String second = header(transport.requests.get(1), "Idempotency-Key");
        assertNotNull(first);
        assertEquals(first, second);
    }
}
