# Anypost Java SDK

The official Java client for the [Anypost](https://anypost.com) email API.

Requires Java 17+. One dependency (Jackson) for JSON; HTTP is the JDK's built-in
client. Instances are immutable and safe to share across threads.

This README covers the SDK itself: installation, idioms, and configuration. For platform concepts and the full field-level API reference, see the [Anypost documentation](https://anypost.com/docs).

## Install

Maven:

```xml
<dependency>
  <groupId>com.anypost</groupId>
  <artifactId>anypost-java</artifactId>
  <version>1.1.0</version>
</dependency>
```

Gradle:

```groovy
implementation("com.anypost:anypost-java:1.1.0")
```

## Quickstart

```java
import com.anypost.Anypost;
import com.anypost.model.SendEmailRequest;
import com.anypost.model.SendResponse;

Anypost client = Anypost.create("ap_your_api_key");

SendResponse sent = client.email.send(SendEmailRequest.builder()
        .from("YourCo <you@yourdomain.com>")
        .to("you@example.com")
        .subject("Welcome to Anypost")
        .html("<p>Hello, inbox!</p>")
        .build());

System.out.println(sent.id());
```

`Anypost.fromEnv()` reads the key from `ANYPOST_API_KEY` instead. Keep the key
server-side; it is a bearer credential.

## Sending

One of `text`, `html`, or `templateId` is required. All addresses in `to`, `cc`,
and `bcc` share one envelope and count against a combined limit of 50.

```java
client.email.send(SendEmailRequest.builder()
        .from("YourCo <you@yourdomain.com>")
        .to("a@example.com", "b@example.com")
        .cc("team@example.com")
        .replyTo("support@yourdomain.com")
        .subject("Receipt #4823")
        .html("<p>Thanks for your order.</p>")
        .text("Thanks for your order.")
        .tags("receipt")
        .build());
```

`Attachment` content is the raw file bytes. Pass what `Files.readAllBytes`
returns and the SDK base64-encodes it on the wire. Do not pre-encode it. The
request body is capped at 5 MB.

```java
byte[] pdf = Files.readAllBytes(Path.of("report.pdf"));

client.email.send(SendEmailRequest.builder()
        .from("YourCo <you@yourdomain.com>")
        .to("someone@example.com")
        .subject("Your report")
        .text("Attached.")
        .attachment(Attachment.of("report.pdf", pdf))
        .build());
```

Send with a published template and per-recipient variables:

```java
client.email.send(SendEmailRequest.builder()
        .from("YourCo <you@yourdomain.com>")
        .to("someone@example.com")
        .templateId("template_018f2c5e-3a40-7a91-9c25-3a0b1d5e6f78")
        .variable("name", "Ada")
        .variable("plan", "pro")
        .build());
```

See the [send reference](https://anypost.com/docs/reference/emails) for the complete field list.

## Batch

Send 1 to 100 independent messages in one request. `defaults` fills any field an
entry omits. Leave an entry's `from` (and any other shared field) unset to inherit
the default; an entry that sets its own value wins. `to` is always per-entry.

```java
BatchResponse result = client.email.sendBatch(EmailBatchRequest.builder()
        .defaults(SendEmailRequest.builder().from("YourCo <you@yourdomain.com>").build())
        .email(SendEmailRequest.builder().to("a@example.com").subject("Hi A").text("...").build())
        .email(SendEmailRequest.builder().to("b@example.com").subject("Hi B").text("...").build())
        .build());
```

A batch with mixed outcomes returns HTTP `207` and does not throw. Inspect each
entry's status rather than treating it as a failure:

```java
System.out.println(result.summary().queued() + "/" + result.summary().total());

for (BatchItemResult entry : result.data()) {
    if (entry.isQueued()) {
        System.out.println(entry.index() + " " + entry.id());
    } else {
        System.out.println(entry.index() + " " + entry.error().type() + " " + entry.error().message());
    }
}
```

## Domains

Manage sending domains under `client.domains`. Add a domain, publish the records
it returns, then verify.

```java
Domain domain = client.domains.create(DomainCreateParams.of("example.com"));
for (DnsRecord r : domain.dnsRecords()) {
    System.out.println(r.type() + " " + r.name() + " -> " + r.value());
}

Domain checked = client.domains.verify(domain.id());
if (!"verified".equals(checked.status())) {
    // verify returns the current domain even while pending; it never throws
    System.out.println(checked.verificationFailure().code());
}
```

`get`, `update` (tracking config only), and `delete` round out the resource. See [Domains](https://anypost.com/docs/reference/domains) for the verification lifecycle and field reference.

## API keys

Manage keys under `client.apiKeys`. The plaintext secret comes back only once, on
`create`, as `key()`:

```java
ApiKeyWithSecret created = client.apiKeys.create(ApiKeyCreateParams.builder()
        .name("Production server")
        .permissions(Permissions.SEND_ONLY)
        .allowedDomains("example.com")
        .build());

System.out.println(created.key()); // store now; never retrievable again
```

`get` returns metadata only (`keyPrefix()`, never the secret); `update` and `delete` round out the resource. See [API keys](https://anypost.com/docs/reference/api-keys) for the permission model and cache propagation.

## Templates

Templates use a draft/published model: edits land in a draft, and `publish`
promotes it. A template can't be used for sending until it's published.

```java
Template tmpl = client.templates.create(TemplateCreateParams.builder()
        .name("Welcome email")
        .kind(TemplateKind.HTML)
        .html("<h1>Welcome, {{ name }}</h1>")
        .build());

client.templates.publish(tmpl.id());
```

`kind` (`html` or `markdown`) is immutable once set. `getDraft`, `updateDraft`,
`deleteDraft`, `duplicate`, `get`, `update` (name only), and `delete` round out
the resource. Send with a published template via `templateId` (see [Sending](#sending)). See [Templates](https://anypost.com/docs/reference/templates) for the full model.

## Suppressions

A suppression blocks sends to an address, scoped to a `topic`. The wildcard `*`
blocks every topic; a named topic (e.g. `marketing`) leaves transactional
traffic untouched.

```java
client.suppressions.create(SuppressionCreateParams.builder()
        .email("alice@example.com")
        .topic("marketing")
        .note("Customer requested removal")
        .build());

client.suppressions.delete("alice@example.com", "marketing");
```

`get`, `list` (with `emailContains`, `topic`, `reason`, and `origin` filters), `listForEmail`, and `deleteForEmail` round out the resource. See [Suppressions](https://anypost.com/docs/reference/suppressions) for scoping and the automatic-suppression rules for bounces and complaints.

## Webhooks

Manage webhook subscriptions under `client.webhooks`. The signing secret comes
back only once, on `create`; later reads return only the prefix.

```java
WebhookWithSecret wh = client.webhooks.create(WebhookCreateParams.builder()
        .name("Production events")
        .url("https://hooks.example.com/anypost")
        .events(WebhookEventType.DELIVERED, WebhookEventType.BOUNCED, WebhookEventType.COMPLAINED)
        .build());

System.out.println(wh.signingSecret()); // store now; never retrievable again
```

`update`, `test`, `rotateSecret`, `get`, `list`, and `delete` round out the resource. See [Webhooks](https://anypost.com/docs/reference/webhooks) for the event catalog, status transitions, and the secret-rotation grace window.

### Verifying deliveries

`WebhookVerifier` has static methods. They need the signing secret, not an
API key, so call them in your handler without a client. Pass the **raw** request
body (the exact bytes, before JSON parsing), the `Anypost-Signature` header, and
the secret.

```java
byte[] body = request.getInputStream().readAllBytes();
String signature = request.getHeader("Anypost-Signature");

try {
    WebhookDelivery delivery = WebhookVerifier.unwrap(body, signature, signingSecret);
    for (WebhookDeliveryEvent event : delivery.events()) {
        // event.type(), event.data().get("email_id"), ...
    }
} catch (WebhookVerificationException e) {
    // e.reason(): NO_MATCH, TIMESTAMP_OUT_OF_TOLERANCE, ...
    response.setStatus(400);
}
```

Reach for `verifySignature` when something else has already parsed the body.
Keep the raw bytes for the verify step, then use your parsed value once it
passes. Deliveries older than five minutes are rejected by default to bound
replay; `WebhookVerifyOptions` widens, narrows, or disables (`Duration.ZERO`) that
check, and overrides the clock in tests. During a secret rotation the header
carries a `v1=` component per active secret, and a match on any one passes, so
deliveries keep verifying while you redeploy.

## Events

`client.events.list` pages the team's event stream, newest-first. The window
defaults to the last 24 hours and is clamped to your plan's retention. Events are
read-only and not addressable by id, so there is no `get`.

```java
Page<Event> page = client.events.list(EventListParams.builder()
        .eventType(EventType.BOUNCED)
        .build());

for (Event e : page.data()) {
    System.out.println(e.occurredAt() + " " + e.recipient() + " " + e.bounceClassification());
}
```

Filter by `start`, `end`, `eventType`, `recipient`, `emailId`, `messageId`,
`domain`, `topic`, `campaign`, `templateId`, and `tags`, which matches an event
carrying *any* of the given tags. Every other filter is exact-match. This is also
how you backfill the gap after a webhook endpoint was disabled: page the events
that occurred during the outage once it's healthy. See [Events](https://anypost.com/docs/reference/events) for the field reference.

## Pagination

List endpoints return a `Page<T>` with `data()`, `hasMore()`, and `nextCursor()`.
Read one page, call `next()` to fetch the following one, or iterate `all()` /
`stream()` to walk every item across pages, re-fetching as it goes.

```java
Page<Domain> page = client.domains.list(ListParams.builder().limit(50).build());
page.data();        // this page's items
page.hasMore();     // whether another page exists
page.nextCursor();  // pass to ListParams.after(...) to fetch it yourself

for (Domain domain : page.all()) { // every domain, across all pages
    System.out.println(domain.name());
}
```

## Errors

A failed request throws an `AnypostException`. Branch on `type()`, the stable,
machine-readable `error.type`, not on the HTTP status.

```java
try {
    client.email.send(message);
} catch (AnypostException e) {
    switch (e.type()) {
        case VALIDATION -> System.out.println(e.validationErrors()); // field -> messages
        case RATE_LIMIT -> System.out.println(e.retryAfter());       // Duration, may be null
        default -> System.out.println(e.type() + " " + e.status() + " " + e.requestId());
    }
}
```

| `ErrorType` | `error.type` | Status |
|---|---|---|
| `VALIDATION` | `validation_error` | `400`, `422` |
| `AUTHENTICATION` | `authentication_error` | `401` |
| `PERMISSION` | `permission_error` | `403` |
| `NOT_FOUND` | `not_found` | `404` |
| `CONFLICT` / `IDEMPOTENCY_CONFLICT` / `WEBHOOK_ROTATION` | `conflict`, `idempotency_concurrent`, `webhook_rotation_in_progress` | `409` |
| `IDEMPOTENCY_MISMATCH` | `idempotency_mismatch` | `422` |
| `RATE_LIMIT` | `rate_limit_exceeded` | `429` |
| `PAYLOAD_TOO_LARGE` | `payload_too_large` | `413` |
| `INTERNAL` / `PROVISIONING` | `internal_error`, `provisioning_error` | `5xx` |
| `API_ERROR` | (unrecognized type) | any |
| `CONNECTION` | (no response) | none |

Every API-level error carries `type()`, `status()`, `requestId()`, the message,
and the raw `body()`. A connection error (no response) carries
`ErrorType.CONNECTION`, a zero status, and the underlying transport error via
`getCause()`.

## Retries and idempotency

The client retries `429`, `502`, `503`, and network failures up to `maxRetries`
times (default 2), with exponential backoff and full jitter. It honors
`Retry-After`.

Sends are made safe to retry automatically: when retries are enabled and you do
not pass an idempotency key, the client generates one and reuses it across
attempts, so a retried send cannot deliver twice. Pass your own key to dedupe
across process restarts:

```java
client.email.send(message, RequestOptions.idempotencyKey("order-4823"));
```

## Configuration

```java
Anypost client = Anypost.builder("ap_your_api_key")
        .baseUrl("https://api.anypost.com/v1")
        .timeout(Duration.ofSeconds(30))
        .maxRetries(2)
        .httpClient(HttpClient.newHttpClient())
        .defaultHeader("X-My-Header", "value")
        .build();
```

| Option | Default | Description |
|---|---|---|
| `baseUrl` | `https://api.anypost.com/v1` | API base URL. |
| `timeout` | 30s | Per-request timeout. |
| `maxRetries` | 2 | Automatic retries for transient failures. |
| `httpClient` | `HttpClient.newHttpClient()` | Custom client/transport (proxy, TLS). |
| `defaultHeader` | none | Extra header sent on every request (repeatable). |

`Anypost.fromEnv()` reads `ANYPOST_API_KEY` from the environment.

## License

MIT
