package com.anypost;

import com.anypost.model.Event;
import com.anypost.model.EventType;
import org.junit.jupiter.api.Test;

import static com.anypost.TestSupport.MockTransport;
import static com.anypost.TestSupport.client;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventsTest {

    @Test
    void exposesBotOnProxiedOpen() {
        String body = "{\"data\":["
                + "{\"id\":\"evt_bot\",\"type\":\"email.opened\","
                + "\"tracking\":{\"bot\":{\"source\":\"google\",\"kind\":\"proxy\"}}},"
                + "{\"id\":\"evt_human\",\"type\":\"email.opened\",\"tracking\":null}"
                + "],\"has_more\":false,\"next_cursor\":null}";
        MockTransport transport = new MockTransport().enqueue(200, body);

        Page<Event> page = client(transport).events.list();

        Event proxied = page.data().get(0);
        assertEquals("google", proxied.tracking().bot().source());
        assertEquals("proxy", proxied.tracking().bot().kind());
        // A human open carries no bot classification.
        assertNull(page.data().get(1).tracking());
    }

    @Test
    void threadsEventTypeAndTagsIntoQuery() {
        MockTransport transport = new MockTransport()
                .enqueue(200, "{\"data\":[],\"has_more\":false,\"next_cursor\":null}");

        client(transport).events.list(EventListParams.builder()
                .eventType(EventType.BOUNCED)
                .tags("welcome", "onboarding")
                .build());

        String uri = transport.lastRequest().uri().toString();
        assertTrue(uri.contains("event_type=email.bounced"), uri);
        // Sent comma-separated (URL-encoded); the API matches with hasAny.
        assertTrue(uri.contains("tags=welcome%2Conboarding") || uri.contains("tags=welcome,onboarding"), uri);
    }

    @Test
    void threadsIpPoolIntoQuery() {
        MockTransport transport = new MockTransport()
                .enqueue(200, "{\"data\":[],\"has_more\":false,\"next_cursor\":null}");

        client(transport).events.list(EventListParams.builder().ipPool("marketing").build());

        String uri = transport.lastRequest().uri().toString();
        assertTrue(uri.contains("ip_pool=marketing"), uri);
    }

    @Test
    void deserializesIpPoolOffAnEvent() {
        String body = "{\"data\":["
                + "{\"id\":\"evt_pooled\",\"type\":\"email.delivered\",\"ip_pool\":\"marketing\"},"
                + "{\"id\":\"evt_shared\",\"type\":\"email.delivered\",\"ip_pool\":null}"
                + "],\"has_more\":false,\"next_cursor\":null}";
        MockTransport transport = new MockTransport().enqueue(200, body);

        Page<Event> page = client(transport).events.list();

        assertEquals("marketing", page.data().get(0).ipPool());
        // Null on sends that named no pool, and on accounts without dedicated IPs.
        assertNull(page.data().get(1).ipPool());
    }
}
