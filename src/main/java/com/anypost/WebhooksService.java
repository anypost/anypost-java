package com.anypost;

import com.anypost.model.Webhook;
import com.anypost.model.WebhookCreateParams;
import com.anypost.model.WebhookTestResult;
import com.anypost.model.WebhookUpdateParams;
import com.anypost.model.WebhookWithSecret;

/** The {@code /webhooks} operations. Access it via {@link Anypost#webhooks}. */
public final class WebhooksService {

    private final RequestExecutor executor;

    WebhooksService(RequestExecutor executor) {
        this.executor = executor;
    }

    /** One page of the team's webhooks, newest-first. */
    public Page<Webhook> list() {
        return list(ListParams.DEFAULT, null);
    }

    public Page<Webhook> list(ListParams params) {
        return list(params, null);
    }

    public Page<Webhook> list(ListParams params, RequestOptions options) {
        return fetchPage(params == null ? ListParams.DEFAULT : params, options);
    }

    private Page<Webhook> fetchPage(ListParams params, RequestOptions options) {
        Query query = new Query();
        params.applyTo(query);
        PageEnvelope<Webhook> envelope =
                executor.request("GET", "/webhooks", null, false, query, options, Json.pageEnvelopeOf(Webhook.class));
        return new Page<>(envelope, cursor -> fetchPage(params.withAfter(cursor), options));
    }

    /**
     * Makes a webhook. The full signing secret is on this response only &mdash;
     * store it now to verify future deliveries; later reads return only the prefix.
     */
    public WebhookWithSecret create(WebhookCreateParams params) {
        return create(params, null);
    }

    public WebhookWithSecret create(WebhookCreateParams params, RequestOptions options) {
        return executor.request("POST", "/webhooks", params, false, null, options, WebhookWithSecret.class);
    }

    /** Retrieves a webhook. The signing secret is never returned &mdash; only its prefix. */
    public Webhook get(String id) {
        return get(id, null);
    }

    public Webhook get(String id, RequestOptions options) {
        return executor.request("GET", "/webhooks/" + Util.encodePath(id), null, false, null, options, Webhook.class);
    }

    /**
     * Changes a webhook's name, URL, events, and status. It does not rotate the
     * signing secret &mdash; use {@link #rotateSecret(String)}.
     */
    public Webhook update(String id, WebhookUpdateParams params) {
        return update(id, params, null);
    }

    public Webhook update(String id, WebhookUpdateParams params, RequestOptions options) {
        return executor.request("PATCH", "/webhooks/" + Util.encodePath(id), params, false, null, options, Webhook.class);
    }

    /** Permanently removes a webhook. */
    public void delete(String id) {
        delete(id, null);
    }

    public void delete(String id, RequestOptions options) {
        executor.requestNoContent("DELETE", "/webhooks/" + Util.encodePath(id), null, options);
    }

    /**
     * Sends one synthetic {@code webhook.test} event and reports the outcome.
     * One-shot, not retried, and absent from delivery history. Returns the result
     * even when the endpoint fails &mdash; read {@link WebhookTestResult#delivered()}.
     * Works on a disabled webhook too.
     */
    public WebhookTestResult test(String id) {
        return test(id, null);
    }

    public WebhookTestResult test(String id, RequestOptions options) {
        return executor.request("POST", "/webhooks/" + Util.encodePath(id) + "/test", null, false, null, options, WebhookTestResult.class);
    }

    /**
     * Rotates the signing secret. The new secret is on this response only. The
     * previous secret stays valid for a 24h grace window. Rotating again before the
     * window ends throws a {@code WEBHOOK_ROTATION} conflict.
     */
    public WebhookWithSecret rotateSecret(String id) {
        return rotateSecret(id, null);
    }

    public WebhookWithSecret rotateSecret(String id, RequestOptions options) {
        return executor.request("POST", "/webhooks/" + Util.encodePath(id) + "/rotate-secret", null, false, null, options, WebhookWithSecret.class);
    }
}
