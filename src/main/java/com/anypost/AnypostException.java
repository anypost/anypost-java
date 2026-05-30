package com.anypost;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * The single exception thrown by every SDK call that fails.
 *
 * <p>A request that reached the API and came back non-2xx carries a {@link #type()},
 * {@link #status()}, and (when sent) a {@link #requestId()}; a request that never
 * got a response carries {@link ErrorType#CONNECTION}, a zero status, and a
 * wrapped cause. Branch on {@link #type()} &mdash; it is the stable contract, the
 * status is not:
 *
 * <pre>{@code
 * try {
 *     client.email.send(message);
 * } catch (AnypostException e) {
 *     switch (e.type()) {
 *         case VALIDATION -> e.validationErrors(); // field -> messages
 *         case RATE_LIMIT -> e.retryAfter();       // Duration, may be null
 *         default -> log.warn("{} ({})", e.getMessage(), e.requestId());
 *     }
 * }
 * }</pre>
 *
 * <p>It extends {@link RuntimeException} so it propagates cleanly through streams
 * and lambdas (for example {@link Page#stream()}).
 */
public final class AnypostException extends RuntimeException {

    private static final List<String> REQUEST_ID_HEADERS =
            List.of("Anypost-Request-Id", "X-Anypost-Request-Id", "X-Request-Id");

    private final ErrorType type;
    private final int status;
    private final String requestId;
    private final Map<String, List<String>> validationErrors;
    private final Duration retryAfter;
    private final byte[] body;

    AnypostException(ErrorType type, String message, int status, String requestId,
                     Map<String, List<String>> validationErrors, Duration retryAfter,
                     byte[] body, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.status = status;
        this.requestId = requestId;
        this.validationErrors = validationErrors;
        this.retryAfter = retryAfter;
        this.body = body;
    }

    /** The stable, machine-readable error type. Branch on this. */
    public ErrorType type() {
        return type;
    }

    /** The HTTP status code, or 0 when no response was received. */
    public int status() {
        return status;
    }

    /** The server-assigned request id, or {@code null}. Quote it in support requests. */
    public String requestId() {
        return requestId;
    }

    /**
     * A map of field path to its list of problems. Populated only for
     * {@link ErrorType#VALIDATION}; {@code null} otherwise.
     */
    public Map<String, List<String>> validationErrors() {
        return validationErrors;
    }

    /**
     * The server-advised wait before retrying. Populated only for
     * {@link ErrorType#RATE_LIMIT} when the response carried a {@code Retry-After}
     * header; {@code null} otherwise.
     */
    public Duration retryAfter() {
        return retryAfter;
    }

    /** The raw response body, or {@code null} for a connection error. */
    public byte[] body() {
        return body;
    }

    // --- factories -------------------------------------------------------------

    /** Builds an exception from a non-2xx HTTP response, keying on the canonical {@code error.type}. */
    static AnypostException fromResponse(int status, byte[] body, HttpHeaders headers) {
        String requestId = readRequestId(headers);

        String errType = null;
        String message = null;
        Map<String, List<String>> fields = null;

        try {
            JsonNode root = Json.MAPPER.readTree(body);
            JsonNode error = root.get("error");
            if (error != null && error.isObject()) {
                // Canonical envelope: {"error": {type, message, errors?}}.
                errType = text(error.get("type"));
                message = text(error.get("message"));
                JsonNode errs = error.get("errors");
                if (errs != null && errs.isObject()) {
                    fields = Json.MAPPER.convertValue(errs, new TypeReference<Map<String, List<String>>>() {});
                }
            } else if (error != null && error.isTextual()) {
                // Flat envelope: {"error": "<code>", "message"?}.
                errType = error.asText();
                message = text(root.get("message"));
                if (message == null) {
                    message = errType.replace('_', ' ');
                }
            }
        } catch (Exception ignored) {
            // Non-JSON or unexpected body: fall back to status-derived classification.
        }

        ErrorType type = errType != null ? ErrorType.from(errType) : typeFromStatus(status);
        if (message == null) {
            message = "Anypost request failed with status " + status + ".";
        }

        Map<String, List<String>> validationErrors = null;
        if (type == ErrorType.VALIDATION || status == 400 || status == 422) {
            validationErrors = fields;
        }
        Duration retryAfter = null;
        if (type == ErrorType.RATE_LIMIT || status == 429) {
            retryAfter = retryAfter(headers);
        }

        return new AnypostException(type, message, status, requestId, validationErrors, retryAfter, body, null);
    }

    /** Builds an exception for a transport failure with no HTTP response. */
    static AnypostException connection(String message, Throwable cause) {
        return new AnypostException(ErrorType.CONNECTION, message, 0, null, null, null, null, cause);
    }

    // --- header parsing --------------------------------------------------------

    private static ErrorType typeFromStatus(int status) {
        return switch (status) {
            case 401 -> ErrorType.AUTHENTICATION;
            case 403 -> ErrorType.PERMISSION;
            case 404 -> ErrorType.NOT_FOUND;
            case 409 -> ErrorType.CONFLICT;
            case 413 -> ErrorType.PAYLOAD_TOO_LARGE;
            case 429 -> ErrorType.RATE_LIMIT;
            case 400, 422 -> ErrorType.VALIDATION;
            default -> status >= 500 ? ErrorType.INTERNAL : ErrorType.API_ERROR;
        };
    }

    private static String readRequestId(HttpHeaders headers) {
        for (String name : REQUEST_ID_HEADERS) {
            String value = headers.firstValue(name).orElse(null);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }

    /** Parses {@code Retry-After} (delta-seconds or HTTP-date) into a positive duration, or {@code null}. */
    static Duration retryAfter(HttpHeaders headers) {
        String value = headers.firstValue("Retry-After").orElse(null);
        if (value == null || value.isBlank()) {
            return null;
        }
        value = value.trim();
        try {
            double seconds = Double.parseDouble(value);
            return seconds >= 0 ? Duration.ofMillis((long) (seconds * 1000)) : null;
        } catch (NumberFormatException notSeconds) {
            try {
                ZonedDateTime when = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME);
                Duration d = Duration.between(ZonedDateTime.now(when.getZone()), when);
                return d.isNegative() || d.isZero() ? null : d;
            } catch (Exception notDate) {
                return null;
            }
        }
    }

    private static String text(JsonNode node) {
        return node != null && node.isTextual() ? node.asText() : null;
    }

    @Override
    public String toString() {
        if (status == 0) {
            return "AnypostException: " + getMessage();
        }
        if (requestId != null) {
            return "AnypostException: " + getMessage() + " (type=" + type.value()
                    + ", status=" + status + ", request_id=" + requestId + ")";
        }
        return "AnypostException: " + getMessage() + " (type=" + type.value() + ", status=" + status + ")";
    }

    /** UTF-8 view of the raw body, or {@code null}. */
    public String bodyString() {
        return body == null ? null : new String(body, StandardCharsets.UTF_8);
    }
}
