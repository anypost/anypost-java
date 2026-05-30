package com.anypost;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.SSLSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

/** Test helpers: a recording transport, a minimal {@link HttpResponse}, and request introspection. */
final class TestSupport {

    static final ObjectMapper MAPPER = new ObjectMapper();

    private TestSupport() {}

    /** A queued, recording {@link HttpTransport}. Enqueue responses or {@link IOException}s in order. */
    static final class MockTransport implements HttpTransport {
        final List<HttpRequest> requests = new ArrayList<>();
        private final Deque<Object> queue = new ArrayDeque<>();

        MockTransport enqueue(int status, String body) {
            queue.add(new FakeResponse(status, body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8),
                    HttpHeaders.of(Map.of(), (a, b) -> true)));
            return this;
        }

        MockTransport enqueue(int status, String body, Map<String, List<String>> headers) {
            queue.add(new FakeResponse(status, body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8),
                    HttpHeaders.of(headers, (a, b) -> true)));
            return this;
        }

        MockTransport enqueueError(IOException error) {
            queue.add(error);
            return this;
        }

        @Override
        public HttpResponse<byte[]> send(HttpRequest request) throws IOException {
            requests.add(request);
            Object next = queue.poll();
            if (next == null) {
                throw new IllegalStateException("MockTransport: no queued response for " + request.method()
                        + " " + request.uri());
            }
            if (next instanceof IOException e) {
                throw e;
            }
            @SuppressWarnings("unchecked")
            HttpResponse<byte[]> response = (HttpResponse<byte[]>) next;
            return response;
        }

        HttpRequest lastRequest() {
            return requests.get(requests.size() - 1);
        }

        int count() {
            return requests.size();
        }
    }

    /** Builds a client wired to the mock, with deterministic (no-op) backoff. */
    static Anypost client(MockTransport transport, int maxRetries) {
        Anypost anypost = Anypost.builder("ap_test_key")
                .maxRetries(maxRetries)
                .baseUrl("https://api.test/v1")
                .transport(transport)
                .build();
        anypost.executor().sleeper = millis -> { /* no real sleep in tests */ };
        anypost.executor().jitter = () -> 1.0;
        return anypost;
    }

    static Anypost client(MockTransport transport) {
        return client(transport, 0);
    }

    /** Drains a request's body publisher into bytes. */
    static byte[] bodyOf(HttpRequest request) {
        if (request.bodyPublisher().isEmpty()) {
            return new byte[0];
        }
        Flow.Publisher<ByteBuffer> publisher = request.bodyPublisher().get();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CountDownLatch done = new CountDownLatch(1);
        publisher.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item) {
                byte[] chunk = new byte[item.remaining()];
                item.get(chunk);
                out.writeBytes(chunk);
            }

            @Override
            public void onError(Throwable throwable) {
                done.countDown();
            }

            @Override
            public void onComplete() {
                done.countDown();
            }
        });
        try {
            done.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return out.toByteArray();
    }

    static String bodyStringOf(HttpRequest request) {
        return new String(bodyOf(request), StandardCharsets.UTF_8);
    }

    static String header(HttpRequest request, String name) {
        return request.headers().firstValue(name).orElse(null);
    }

    /** A minimal {@link HttpResponse} carrying only status, headers, and body. */
    record FakeResponse(int status, byte[] bytes, HttpHeaders headers) implements HttpResponse<byte[]> {
        @Override
        public int statusCode() {
            return status;
        }

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public Optional<HttpResponse<byte[]>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return headers;
        }

        @Override
        public byte[] body() {
            return bytes;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return URI.create("https://api.test/v1");
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}
