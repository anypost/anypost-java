package com.anypost;

import com.anypost.model.SendEmailRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.anypost.TestSupport.MockTransport;
import static com.anypost.TestSupport.client;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorsTest {

    private static final SendEmailRequest MSG =
            SendEmailRequest.builder().from("a@example.com").to("b@example.com").subject("S").text("T").build();

    private static AnypostException sendExpectingError(MockTransport transport) {
        return assertThrows(AnypostException.class, () -> client(transport, 0).email.send(MSG));
    }

    @Test
    void validationErrorExposesFieldMap() {
        MockTransport transport = new MockTransport().enqueue(422,
                "{\"error\":{\"type\":\"validation_error\",\"message\":\"Invalid\",\"errors\":{\"to\":[\"is required\"]}}}");
        AnypostException e = sendExpectingError(transport);
        assertEquals(ErrorType.VALIDATION, e.type());
        assertEquals(422, e.status());
        assertNotNull(e.validationErrors());
        assertEquals("is required", e.validationErrors().get("to").get(0));
    }

    @Test
    void authenticationError() {
        MockTransport transport = new MockTransport().enqueue(401,
                "{\"error\":{\"type\":\"authentication_error\",\"message\":\"bad key\"}}");
        assertEquals(ErrorType.AUTHENTICATION, sendExpectingError(transport).type());
    }

    @Test
    void notFoundError() {
        MockTransport transport = new MockTransport().enqueue(404,
                "{\"error\":{\"type\":\"not_found\",\"message\":\"nope\"}}");
        assertEquals(ErrorType.NOT_FOUND, sendExpectingError(transport).type());
    }

    @Test
    void flatPayloadTooLargeForm() {
        MockTransport transport = new MockTransport().enqueue(413, "{\"error\":\"payload_too_large\"}");
        AnypostException e = sendExpectingError(transport);
        assertEquals(ErrorType.PAYLOAD_TOO_LARGE, e.type());
        assertEquals("payload too large", e.getMessage());
    }

    @Test
    void rateLimitCapturesRetryAfter() {
        MockTransport transport = new MockTransport().enqueue(429,
                "{\"error\":{\"type\":\"rate_limit_exceeded\",\"message\":\"slow down\"}}",
                Map.of("Retry-After", List.of("12")));
        AnypostException e = sendExpectingError(transport);
        assertEquals(ErrorType.RATE_LIMIT, e.type());
        assertNotNull(e.retryAfter());
        assertEquals(12, e.retryAfter().getSeconds());
    }

    @Test
    void capturesRequestId() {
        MockTransport transport = new MockTransport().enqueue(500,
                "{\"error\":{\"type\":\"internal_error\",\"message\":\"boom\"}}",
                Map.of("Anypost-Request-Id", List.of("req_abc")));
        AnypostException e = sendExpectingError(transport);
        assertEquals(ErrorType.INTERNAL, e.type());
        assertEquals("req_abc", e.requestId());
    }

    @Test
    void unknownTypeFallsBackToApiError() {
        MockTransport transport = new MockTransport().enqueue(418,
                "{\"error\":{\"type\":\"i_am_a_teapot\",\"message\":\"short and stout\"}}");
        AnypostException e = sendExpectingError(transport);
        assertEquals(ErrorType.API_ERROR, e.type());
        assertEquals("short and stout", e.getMessage());
    }

    @Test
    void connectionErrorWhenTransportFails() {
        MockTransport transport = new MockTransport().enqueueError(new IOException("connection refused"));
        AnypostException e = sendExpectingError(transport);
        assertEquals(ErrorType.CONNECTION, e.type());
        assertEquals(0, e.status());
        assertNotNull(e.getCause());
        assertTrue(e.getCause() instanceof IOException);
    }
}
