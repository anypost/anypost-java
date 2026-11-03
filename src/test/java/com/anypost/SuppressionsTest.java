package com.anypost;

import com.anypost.model.SuppressionBatchItem;
import com.anypost.model.SuppressionBatchResult;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.anypost.TestSupport.MockTransport;
import static com.anypost.TestSupport.client;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class SuppressionsTest {

    @Test
    void createBatchPostsWrappedItemsAndParsesResult() throws Exception {
        MockTransport transport = new MockTransport().enqueue(200,
                "{\"imported\":1,\"already_suppressed\":0,\"invalid\":[{\"index\":1,\"email\":\"not-an-address\"}]}");

        SuppressionBatchResult result = client(transport).suppressions.createBatch(List.of(
                SuppressionBatchItem.builder().email("alice@example.com").build(),
                SuppressionBatchItem.builder().email("not-an-address").build(),
                SuppressionBatchItem.builder().email("bob@example.com").topic("marketing")
                        .reason("hard bounce").note("n").build()));

        assertEquals(1, result.imported());
        assertEquals(0, result.alreadySuppressed());
        assertEquals(1, result.invalid().size());
        assertEquals(1, result.invalid().get(0).index());
        assertEquals("not-an-address", result.invalid().get(0).email());

        var request = transport.lastRequest();
        assertEquals("POST", request.method());
        assertEquals("https://api.test/v1/suppressions/batch", request.uri().toString());

        JsonNode body = TestSupport.MAPPER.readTree(TestSupport.bodyOf(request));
        JsonNode items = body.get("suppressions");
        assertEquals(3, items.size());
        assertEquals("alice@example.com", items.get(0).get("email").asText());
        assertFalse(items.get(0).has("topic"));
        assertFalse(items.get(0).has("reason"));
        assertFalse(items.get(0).has("note"));
        assertEquals("marketing", items.get(2).get("topic").asText());
        assertEquals("hard bounce", items.get(2).get("reason").asText());
        assertEquals("n", items.get(2).get("note").asText());
    }

    @Test
    void createBatchParsesNullInvalidEmail() {
        MockTransport transport = new MockTransport().enqueue(200,
                "{\"imported\":0,\"already_suppressed\":1,\"invalid\":[{\"index\":0,\"email\":null}]}");
        SuppressionBatchResult result = client(transport).suppressions
                .createBatch(List.of(SuppressionBatchItem.builder().build()));
        assertEquals(1, result.alreadySuppressed());
        assertNull(result.invalid().get(0).email());
    }
}
