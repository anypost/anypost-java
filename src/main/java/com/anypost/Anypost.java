package com.anypost;

import com.anypost.model.WhoamiResponse;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The entry point to the Anypost API.
 *
 * <p>Construct it with {@link #create(String)} (or {@link #fromEnv()} to read
 * {@code ANYPOST_API_KEY}), then call resource methods through the public fields:
 *
 * <pre>{@code
 * Anypost client = Anypost.create("ap_your_api_key");
 *
 * SendResponse sent = client.email.send(SendEmailRequest.builder()
 *         .from("Acme <you@yourdomain.com>")
 *         .to("someone@example.com")
 *         .subject("Hello")
 *         .html("<p>It worked.</p>")
 *         .build());
 * }</pre>
 *
 * <p>A failed call throws an {@link AnypostException}; branch on its {@link AnypostException#type()}.
 * Keep the API key server-side; it is a bearer credential. Instances are
 * immutable and safe to share across threads.
 */
public final class Anypost {

    static final String DEFAULT_BASE_URL = "https://api.anypost.com/v1";
    static final String ENV_API_KEY = "ANYPOST_API_KEY";

    /** Send operations ({@code /email}, {@code /email/batch}). */
    public final EmailService email;
    /** Sending-domain operations ({@code /domains}). */
    public final DomainsService domains;
    /** API-key operations ({@code /api-keys}). */
    public final ApiKeysService apiKeys;
    /** Template operations ({@code /templates}), including draft/publish. */
    public final TemplatesService templates;
    /** Suppression-list operations ({@code /suppressions}). */
    public final SuppressionsService suppressions;
    /** Webhook operations ({@code /webhooks}), including test and rotation. */
    public final WebhooksService webhooks;
    /** Read access to the event stream ({@code /events}). */
    public final EventsService events;

    private final IdentityService identity;
    private final RequestExecutor executor;

    private Anypost(RequestExecutor executor) {
        this.executor = executor;
        this.email = new EmailService(executor);
        this.domains = new DomainsService(executor);
        this.apiKeys = new ApiKeysService(executor);
        this.templates = new TemplatesService(executor);
        this.suppressions = new SuppressionsService(executor);
        this.webhooks = new WebhooksService(executor);
        this.events = new EventsService(executor);
        this.identity = new IdentityService(executor);
    }

    /** Creates a client with an explicit API key. */
    public static Anypost create(String apiKey) {
        return builder(apiKey).build();
    }

    /** Creates a client, reading the API key from {@code ANYPOST_API_KEY}. */
    public static Anypost fromEnv() {
        return builder().build();
    }

    /** A builder seeded with an explicit API key. */
    public static Builder builder(String apiKey) {
        return new Builder(apiKey);
    }

    /** A builder that reads the API key from {@code ANYPOST_API_KEY} at build time. */
    public static Builder builder() {
        return new Builder(null);
    }

    /** Identifies the team and permission level behind the current API key. */
    public WhoamiResponse whoami() {
        return identity.whoami();
    }

    public WhoamiResponse whoami(RequestOptions options) {
        return identity.whoami(options);
    }

    /** Package-private accessor for tests to tune retry timing deterministically. */
    RequestExecutor executor() {
        return executor;
    }

    /** Configures and builds an {@link Anypost} client. */
    public static final class Builder {
        private String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;
        private Duration timeout = Duration.ofSeconds(30);
        private int maxRetries = 2;
        private HttpClient httpClient;
        private HttpTransport transport;
        private final Map<String, String> defaultHeaders = new LinkedHashMap<>();

        private Builder(String apiKey) {
            this.apiKey = apiKey;
        }

        /** Overrides the API base URL. Defaults to the production endpoint. */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Sets the per-request timeout (default 30s), composed with the call. A zero
         * or negative duration disables the client-imposed timeout.
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Sets the number of automatic retries for transient failures (429/502/503
         * and network errors). Defaults to 2. Set 0 to disable.
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Supplies a custom {@link HttpClient} (proxy, custom TLS, executor). The
         * per-request timeout still applies on top.
         */
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /** Adds a header sent on every request. */
        public Builder defaultHeader(String name, String value) {
            this.defaultHeaders.put(name, value);
            return this;
        }

        /** Package-private: inject a fake transport for tests. */
        Builder transport(HttpTransport transport) {
            this.transport = transport;
            return this;
        }

        public Anypost build() {
            String key = apiKey;
            if (key == null || key.isEmpty()) {
                key = System.getenv(ENV_API_KEY);
            }
            if (key == null || key.isEmpty()) {
                throw new IllegalArgumentException(
                        "anypost: an API key is required; pass it to Anypost.create or set " + ENV_API_KEY);
            }

            HttpTransport resolved = transport;
            if (resolved == null) {
                HttpClient client = httpClient != null ? httpClient : HttpClient.newHttpClient();
                resolved = request -> client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            }

            RequestExecutor executor =
                    new RequestExecutor(key, baseUrl, timeout, maxRetries, defaultHeaders, resolved);
            return new Anypost(executor);
        }
    }
}
