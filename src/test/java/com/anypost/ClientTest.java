package com.anypost;

import com.anypost.model.Permissions;
import com.anypost.model.WhoamiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static com.anypost.TestSupport.MockTransport;
import static com.anypost.TestSupport.client;
import static com.anypost.TestSupport.header;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientTest {

    @Test
    void buildingWithoutAKeyOrEnvFails() {
        // An explicit empty key with no ANYPOST_API_KEY set must fail fast.
        if (System.getenv("ANYPOST_API_KEY") == null) {
            assertThrows(IllegalArgumentException.class, () -> Anypost.create(""));
        }
    }

    @Test
    void assemblesAuthAndDefaultHeaders() throws Exception {
        MockTransport transport = new MockTransport()
                .enqueue(200, "{\"team\":{\"id\":\"team_1\",\"name\":\"Acme\"},\"api_key\":{\"id\":\"key_1\",\"permissions\":\"full\"}}");
        Anypost anypost = Anypost.builder("ap_secret")
                .baseUrl("https://api.test/v1")
                .maxRetries(0)
                .defaultHeader("X-Custom", "yes")
                .transport(transport)
                .build();

        WhoamiResponse me = anypost.whoami();

        assertEquals("team_1", me.team().id());
        assertEquals(Permissions.FULL, me.apiKey().permissions());

        var request = transport.lastRequest();
        assertEquals("GET", request.method());
        assertEquals("https://api.test/v1/whoami", request.uri().toString());
        assertEquals("Bearer ap_secret", header(request, "Authorization"));
        assertEquals("application/json", header(request, "Accept"));
        assertEquals("yes", header(request, "X-Custom"));
        assertTrue(header(request, "User-Agent").startsWith("anypost-java/"));
        // A GET carries no body and no Content-Type.
        assertNull(header(request, "Content-Type"));
    }

    @Test
    void deserializesNullTeam() {
        MockTransport transport = new MockTransport()
                .enqueue(200, "{\"team\":null,\"api_key\":{\"id\":\"key_1\",\"permissions\":\"send_only\"}}");
        WhoamiResponse me = client(transport).whoami();
        assertNull(me.team());
        assertEquals(Permissions.SEND_ONLY, me.apiKey().permissions());
    }

    @Test
    void omitsNullFieldsInRequestBodies() throws Exception {
        MockTransport transport = new MockTransport().enqueue(202, "{\"id\":\"email_1\",\"created_at\":\"t\"}");
        Anypost anypost = client(transport);
        anypost.email.send(com.anypost.model.SendEmailRequest.builder()
                .from("a@example.com").to("b@example.com").subject("Hi").text("Yo").build());

        JsonNode body = TestSupport.MAPPER.readTree(TestSupport.bodyOf(transport.lastRequest()));
        assertNotNull(body.get("from"));
        assertNull(body.get("html"), "unset html must be omitted, not null");
        assertNull(body.get("cc"), "unset cc must be omitted");
    }
}
