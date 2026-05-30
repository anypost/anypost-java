package com.anypost;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.function.LongConsumer;

/**
 * Owns the transport and the request loop: header assembly, retries with
 * full-jitter backoff, idempotency keys, and error mapping. Shared by every
 * resource service.
 */
final class RequestExecutor {

    private static final long BASE_BACKOFF_MS = 500;
    private static final long MAX_BACKOFF_MS = 8_000;
    private static final Set<Integer> RETRYABLE = Set.of(429, 502, 503);

    private final String apiKey;
    private final String baseUrl;
    private final Duration timeout;
    private final int maxRetries;
    private final Map<String, String> defaultHeaders;
    private final HttpTransport transport;

    /** Injectable for tests; default to {@link Thread#sleep} and a real PRNG. */
    LongConsumer sleeper = millis -> {
        try {
            if (millis > 0) {
                Thread.sleep(millis);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    };

    DoubleSupplier jitter = Math::random;

    RequestExecutor(String apiKey, String baseUrl, Duration timeout, int maxRetries,
                    Map<String, String> defaultHeaders, HttpTransport transport) {
        this.apiKey = apiKey;
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.timeout = timeout;
        this.maxRetries = maxRetries;
        this.defaultHeaders = new LinkedHashMap<>(defaultHeaders);
        this.transport = transport;
    }

    <T> T request(String method, String path, Object body, boolean idempotent,
                  Query query, RequestOptions options, Class<T> type) {
        return request(method, path, body, idempotent, query, options, Json.typeOf(type));
    }

    <T> T request(String method, String path, Object body, boolean idempotent,
                  Query query, RequestOptions options, JavaType type) {
        byte[] responseBody = execute(method, path, body, idempotent, query, options);
        if (responseBody == null || responseBody.length == 0) {
            return null;
        }
        try {
            return Json.MAPPER.readValue(responseBody, type);
        } catch (IOException e) {
            throw AnypostException.connection("decoding response: " + e.getMessage(), e);
        }
    }

    void requestNoContent(String method, String path, Object body, RequestOptions options) {
        execute(method, path, body, false, null, options);
    }

    /** Runs the request with retries and returns the raw 2xx body, or throws. */
    byte[] execute(String method, String path, Object body, boolean idempotent,
                   Query query, RequestOptions options) {
        byte[] payload = null;
        if (body != null) {
            try {
                payload = Json.MAPPER.writeValueAsBytes(body);
            } catch (JsonProcessingException e) {
                throw AnypostException.connection("encoding request body: " + e.getMessage(), e);
            }
        }

        String url = baseUrl + path;
        if (query != null && !query.isEmpty()) {
            url += "?" + query.encode();
        }
        Map<String, String> headers = buildHeaders(body != null, idempotent, options);

        for (int attempt = 0; ; attempt++) {
            HttpResponse<byte[]> response;
            try {
                response = transport.send(buildRequest(method, url, payload, headers));
            } catch (IOException e) {
                // Transport failure (includes timeout): retry while attempts remain.
                if (attempt < maxRetries) {
                    backoff(attempt, null);
                    continue;
                }
                String message = (e instanceof HttpTimeoutException)
                        ? "Request timed out before a response."
                        : "Could not reach Anypost: " + e.getMessage();
                throw AnypostException.connection(message, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw AnypostException.connection("Request was interrupted before a response.", e);
            }

            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                return response.body();
            }
            if (RETRYABLE.contains(status) && attempt < maxRetries) {
                backoff(attempt, response.headers());
                continue;
            }
            throw AnypostException.fromResponse(status, response.body(), response.headers());
        }
    }

    private HttpRequest buildRequest(String method, String url, byte[] payload, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        if (timeout != null && !timeout.isZero() && !timeout.isNegative()) {
            builder.timeout(timeout);
        }
        HttpRequest.BodyPublisher publisher = payload != null
                ? HttpRequest.BodyPublishers.ofByteArray(payload)
                : HttpRequest.BodyPublishers.noBody();
        builder.method(method, publisher);
        headers.forEach(builder::header);
        return builder.build();
    }

    private Map<String, String> buildHeaders(boolean hasBody, boolean idempotent, RequestOptions options) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Accept", "application/json");
        headers.put("User-Agent", userAgent());
        headers.putAll(defaultHeaders);
        if (hasBody) {
            headers.put("Content-Type", "application/json");
        }
        if (idempotent) {
            String key = options == null ? null : options.idempotencyKeyValue();
            if (key != null && !key.isEmpty()) {
                headers.put("Idempotency-Key", key);
            } else if (maxRetries > 0) {
                // Auto-key so built-in retries of a send cannot deliver twice.
                headers.put("Idempotency-Key", UUID.randomUUID().toString());
            }
        }
        if (options != null) {
            headers.putAll(options.headerMap());
        }
        return headers;
    }

    /** Sleeps before the next retry: Retry-After when present, else full-jitter exponential backoff. */
    private void backoff(int attempt, HttpHeaders headers) {
        long millis;
        Duration retryAfter = headers == null ? null : AnypostException.retryAfter(headers);
        if (retryAfter != null) {
            millis = Math.min(retryAfter.toMillis(), MAX_BACKOFF_MS);
        } else {
            long ceiling = Math.min(BASE_BACKOFF_MS * (1L << attempt), MAX_BACKOFF_MS);
            millis = (long) (jitter.getAsDouble() * ceiling);
        }
        sleeper.accept(millis);
    }

    private static String userAgent() {
        return "anypost-java/" + Version.VERSION + " (Java/" + System.getProperty("java.version") + ")";
    }

    private static String stripTrailingSlash(String url) {
        int end = url.length();
        while (end > 0 && url.charAt(end - 1) == '/') {
            end--;
        }
        return url.substring(0, end);
    }
}
