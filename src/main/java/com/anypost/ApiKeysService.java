package com.anypost;

import com.anypost.model.ApiKey;
import com.anypost.model.ApiKeyCreateParams;
import com.anypost.model.ApiKeyUpdateParams;
import com.anypost.model.ApiKeyWithSecret;

/** The {@code /api-keys} operations. Access it via {@link Anypost#apiKeys}. */
public final class ApiKeysService {

    private final RequestExecutor executor;

    ApiKeysService(RequestExecutor executor) {
        this.executor = executor;
    }

    /** One page of the team's API keys, newest-first. */
    public Page<ApiKey> list() {
        return list(ListParams.DEFAULT, null);
    }

    public Page<ApiKey> list(ListParams params) {
        return list(params, null);
    }

    public Page<ApiKey> list(ListParams params, RequestOptions options) {
        return fetchPage(params == null ? ListParams.DEFAULT : params, options);
    }

    private Page<ApiKey> fetchPage(ListParams params, RequestOptions options) {
        Query query = new Query();
        params.applyTo(query);
        PageEnvelope<ApiKey> envelope =
                executor.request("GET", "/api-keys", null, false, query, options, Json.pageEnvelopeOf(ApiKey.class));
        return new Page<>(envelope, cursor -> fetchPage(params.withAfter(cursor), options));
    }

    /**
     * Issues a new API key. The plaintext secret is returned only in this
     * response, as {@link ApiKeyWithSecret#key()} &mdash; store it securely; it
     * cannot be retrieved later.
     */
    public ApiKeyWithSecret create(ApiKeyCreateParams params) {
        return create(params, null);
    }

    public ApiKeyWithSecret create(ApiKeyCreateParams params, RequestOptions options) {
        return executor.request("POST", "/api-keys", params, false, null, options, ApiKeyWithSecret.class);
    }

    /** Retrieves a single API key's metadata. The secret is never returned. */
    public ApiKey get(String id) {
        return get(id, null);
    }

    public ApiKey get(String id, RequestOptions options) {
        return executor.request("GET", "/api-keys/" + Util.encodePath(id), null, false, null, options, ApiKey.class);
    }

    /**
     * Changes a key's name, permissions, and restrictions. The secret is not
     * rotated here. Changes may take up to 5 minutes to propagate.
     */
    public ApiKey update(String id, ApiKeyUpdateParams params) {
        return update(id, params, null);
    }

    public ApiKey update(String id, ApiKeyUpdateParams params, RequestOptions options) {
        return executor.request("PATCH", "/api-keys/" + Util.encodePath(id), params, false, null, options, ApiKey.class);
    }

    /** Removes a key. It may keep authenticating for up to 5 minutes due to caching. */
    public void delete(String id) {
        delete(id, null);
    }

    public void delete(String id, RequestOptions options) {
        executor.requestNoContent("DELETE", "/api-keys/" + Util.encodePath(id), null, options);
    }
}
