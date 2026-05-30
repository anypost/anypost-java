package com.anypost;

import com.anypost.model.Domain;
import com.anypost.model.DomainCreateParams;
import com.anypost.model.DomainUpdateParams;

/** The {@code /domains} operations. Access it via {@link Anypost#domains}. */
public final class DomainsService {

    private final RequestExecutor executor;

    DomainsService(RequestExecutor executor) {
        this.executor = executor;
    }

    /** One page of the team's domains, newest-first. */
    public Page<Domain> list() {
        return list(ListParams.DEFAULT, null);
    }

    public Page<Domain> list(ListParams params) {
        return list(params, null);
    }

    public Page<Domain> list(ListParams params, RequestOptions options) {
        return fetchPage(params == null ? ListParams.DEFAULT : params, options);
    }

    private Page<Domain> fetchPage(ListParams params, RequestOptions options) {
        Query query = new Query();
        params.applyTo(query);
        PageEnvelope<Domain> envelope =
                executor.request("GET", "/domains", null, false, query, options, Json.pageEnvelopeOf(Domain.class));
        return new Page<>(envelope, cursor -> fetchPage(params.withAfter(cursor), options));
    }

    /** Adds a sending domain. The returned domain is pending until verified. */
    public Domain create(DomainCreateParams params) {
        return create(params, null);
    }

    public Domain create(DomainCreateParams params, RequestOptions options) {
        return executor.request("POST", "/domains", params, false, null, options, Domain.class);
    }

    /** Retrieves a single domain by id. */
    public Domain get(String id) {
        return get(id, null);
    }

    public Domain get(String id, RequestOptions options) {
        return executor.request("GET", "/domains/" + Util.encodePath(id), null, false, null, options, Domain.class);
    }

    /** Changes a domain's tracking configuration. The domain name is immutable. */
    public Domain update(String id, DomainUpdateParams params) {
        return update(id, params, null);
    }

    public Domain update(String id, DomainUpdateParams params, RequestOptions options) {
        return executor.request("PATCH", "/domains/" + Util.encodePath(id), params, false, null, options, Domain.class);
    }

    /** Permanently removes a domain and its DKIM keys. */
    public void delete(String id) {
        delete(id, null);
    }

    public void delete(String id, RequestOptions options) {
        executor.requestNoContent("DELETE", "/domains/" + Util.encodePath(id), null, options);
    }

    /**
     * Triggers a verification check. Always returns the current domain &mdash; read
     * its status and verification failure to learn the outcome; a still-pending
     * domain is not an error. Safe to poll while DNS propagates.
     */
    public Domain verify(String id) {
        return verify(id, null);
    }

    public Domain verify(String id, RequestOptions options) {
        return executor.request("POST", "/domains/" + Util.encodePath(id) + "/verify", null, false, null, options, Domain.class);
    }
}
