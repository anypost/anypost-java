package com.anypost;

import com.anypost.model.Domain;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.anypost.TestSupport.MockTransport;
import static com.anypost.TestSupport.client;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginationTest {

    private static final String PAGE1 =
            "{\"data\":[{\"id\":\"domain_1\",\"name\":\"first.com\"}],\"has_more\":true,\"next_cursor\":\"c2\"}";
    private static final String PAGE2 =
            "{\"data\":[{\"id\":\"domain_2\",\"name\":\"second.com\"}],\"has_more\":false,\"next_cursor\":null}";

    @Test
    void readsOnePageAndFollowsNext() {
        MockTransport transport = new MockTransport().enqueue(200, PAGE1).enqueue(200, PAGE2);
        Page<Domain> page = client(transport).domains.list();

        assertEquals(1, page.data().size());
        assertEquals("first.com", page.data().get(0).name());
        assertTrue(page.hasMore());
        assertEquals("c2", page.nextCursor());

        Page<Domain> next = page.next();
        assertEquals("second.com", next.data().get(0).name());
        assertFalse(next.hasMore());
        assertNull(next.next());
    }

    @Test
    void iteratesEveryItemAcrossPages() {
        MockTransport transport = new MockTransport().enqueue(200, PAGE1).enqueue(200, PAGE2);
        List<String> names = new ArrayList<>();
        for (Domain d : client(transport).domains.list().all()) {
            names.add(d.name());
        }
        assertEquals(List.of("first.com", "second.com"), names);
    }

    @Test
    void streamsEveryItemAcrossPages() {
        MockTransport transport = new MockTransport().enqueue(200, PAGE1).enqueue(200, PAGE2);
        long count = client(transport).domains.list().stream().count();
        assertEquals(2, count);
    }

    @Test
    void appliesLimitAndCursorToQuery() {
        MockTransport transport = new MockTransport()
                .enqueue(200, "{\"data\":[],\"has_more\":false,\"next_cursor\":null}");
        client(transport).domains.list(ListParams.builder().limit(50).after("cur").build());

        String uri = transport.lastRequest().uri().toString();
        assertTrue(uri.contains("limit=50"), uri);
        assertTrue(uri.contains("after=cur"), uri);
    }
}
