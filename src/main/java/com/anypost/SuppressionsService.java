package com.anypost;

import com.anypost.model.Suppression;
import com.anypost.model.SuppressionCreateParams;

import java.util.List;

/**
 * The {@code /suppressions} operations. Entries key on {@code (email, topic)}.
 * Access it via {@link Anypost#suppressions}.
 */
public final class SuppressionsService {

    private final RequestExecutor executor;

    SuppressionsService(RequestExecutor executor) {
        this.executor = executor;
    }

    /** One page of the team's suppressions, newest-first. Expired rows are filtered out. */
    public Page<Suppression> list() {
        return list(SuppressionListParams.DEFAULT, null);
    }

    public Page<Suppression> list(SuppressionListParams params) {
        return list(params, null);
    }

    public Page<Suppression> list(SuppressionListParams params, RequestOptions options) {
        return fetchPage(params == null ? SuppressionListParams.DEFAULT : params, options);
    }

    private Page<Suppression> fetchPage(SuppressionListParams params, RequestOptions options) {
        Query query = new Query();
        params.applyTo(query);
        PageEnvelope<Suppression> envelope =
                executor.request("GET", "/suppressions", null, false, query, options, Json.pageEnvelopeOf(Suppression.class));
        return new Page<>(envelope, cursor -> fetchPage(params.withAfter(cursor), options));
    }

    /**
     * Adds a manual suppression. Defaults to topic {@code "*"} (every topic). Throws
     * a validation error if an active entry for the same {@code (email, topic)} exists.
     */
    public Suppression create(SuppressionCreateParams params) {
        return create(params, null);
    }

    public Suppression create(SuppressionCreateParams params, RequestOptions options) {
        return executor.request("POST", "/suppressions", params, false, null, options, Suppression.class);
    }

    /**
     * Retrieves the suppression for an {@code (email, topic)} pair. Use {@code "*"}
     * for the global row. Throws {@code NOT_FOUND} if the pair isn't suppressed.
     */
    public Suppression get(String email, String topic) {
        return get(email, topic, null);
    }

    public Suppression get(String email, String topic, RequestOptions options) {
        return executor.request("GET",
                "/suppressions/" + Util.encodePath(email) + "/" + Util.encodePath(topic),
                null, false, null, options, Suppression.class);
    }

    /** Removes the single {@code (email, topic)} row. Other topics are untouched. */
    public void delete(String email, String topic) {
        delete(email, topic, null);
    }

    public void delete(String email, String topic, RequestOptions options) {
        executor.requestNoContent("DELETE",
                "/suppressions/" + Util.encodePath(email) + "/" + Util.encodePath(topic), null, options);
    }

    /**
     * Returns every suppression on file for an address, across all topics. Throws
     * {@code NOT_FOUND} if the address has no active suppressions.
     */
    public List<Suppression> listForEmail(String email) {
        return listForEmail(email, null);
    }

    public List<Suppression> listForEmail(String email, RequestOptions options) {
        PageEnvelope<Suppression> envelope = executor.request("GET",
                "/suppressions/" + Util.encodePath(email), null, false, null, options,
                Json.pageEnvelopeOf(Suppression.class));
        return envelope.data() == null ? List.of() : envelope.data();
    }

    /** Removes an address from the suppression list across every topic. */
    public void deleteForEmail(String email) {
        deleteForEmail(email, null);
    }

    public void deleteForEmail(String email, RequestOptions options) {
        executor.requestNoContent("DELETE", "/suppressions/" + Util.encodePath(email), null, options);
    }
}
