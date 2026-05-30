package com.anypost;

import com.anypost.model.Template;
import com.anypost.model.TemplateCreateParams;
import com.anypost.model.TemplateDraft;
import com.anypost.model.TemplateDraftParams;
import com.anypost.model.TemplateDuplicateParams;
import com.anypost.model.TemplateUpdateParams;

/**
 * The {@code /templates} operations, including the draft/publish flow. Access it
 * via {@link Anypost#templates}.
 */
public final class TemplatesService {

    private final RequestExecutor executor;

    TemplatesService(RequestExecutor executor) {
        this.executor = executor;
    }

    /** One page of the team's templates, newest-first. */
    public Page<Template> list() {
        return list(ListParams.DEFAULT, null);
    }

    public Page<Template> list(ListParams params) {
        return list(params, null);
    }

    public Page<Template> list(ListParams params, RequestOptions options) {
        return fetchPage(params == null ? ListParams.DEFAULT : params, options);
    }

    private Page<Template> fetchPage(ListParams params, RequestOptions options) {
        Query query = new Query();
        params.applyTo(query);
        PageEnvelope<Template> envelope =
                executor.request("GET", "/templates", null, false, query, options, Json.pageEnvelopeOf(Template.class));
        return new Page<>(envelope, cursor -> fetchPage(params.withAfter(cursor), options));
    }

    /** Makes a template. It starts unpublished &mdash; publish it before sending. */
    public Template create(TemplateCreateParams params) {
        return create(params, null);
    }

    public Template create(TemplateCreateParams params, RequestOptions options) {
        return executor.request("POST", "/templates", params, false, null, options, Template.class);
    }

    /** Retrieves a template, including its published content. */
    public Template get(String id) {
        return get(id, null);
    }

    public Template get(String id, RequestOptions options) {
        return executor.request("GET", "/templates/" + Util.encodePath(id), null, false, null, options, Template.class);
    }

    /** Changes a template's name. Body content lives on the draft. */
    public Template update(String id, TemplateUpdateParams params) {
        return update(id, params, null);
    }

    public Template update(String id, TemplateUpdateParams params, RequestOptions options) {
        return executor.request("PATCH", "/templates/" + Util.encodePath(id), params, false, null, options, Template.class);
    }

    /** Permanently removes a template. */
    public void delete(String id) {
        delete(id, null);
    }

    public void delete(String id, RequestOptions options) {
        executor.requestNoContent("DELETE", "/templates/" + Util.encodePath(id), null, options);
    }

    /**
     * Copies a template. The copy starts unpublished with a draft seeded from the
     * source's current editable content. Pass {@code null} params for the default name.
     */
    public Template duplicate(String id, TemplateDuplicateParams params) {
        return duplicate(id, params, null);
    }

    public Template duplicate(String id, TemplateDuplicateParams params, RequestOptions options) {
        return executor.request("POST", "/templates/" + Util.encodePath(id) + "/duplicate", params, false, null, options, Template.class);
    }

    /** Retrieves the template's unpublished draft. Throws {@code NOT_FOUND} if none exists. */
    public TemplateDraft getDraft(String id) {
        return getDraft(id, null);
    }

    public TemplateDraft getDraft(String id, RequestOptions options) {
        return executor.request("GET", "/templates/" + Util.encodePath(id) + "/draft", null, false, null, options, TemplateDraft.class);
    }

    /** Creates or updates the template's draft. Idempotent upsert; published content is untouched. */
    public TemplateDraft updateDraft(String id, TemplateDraftParams params) {
        return updateDraft(id, params, null);
    }

    public TemplateDraft updateDraft(String id, TemplateDraftParams params, RequestOptions options) {
        return executor.request("PATCH", "/templates/" + Util.encodePath(id) + "/draft", params, false, null, options, TemplateDraft.class);
    }

    /** Discards the template's draft without touching published content. */
    public void deleteDraft(String id) {
        deleteDraft(id, null);
    }

    public void deleteDraft(String id, RequestOptions options) {
        executor.requestNoContent("DELETE", "/templates/" + Util.encodePath(id) + "/draft", null, options);
    }

    /** Promotes the draft into the published slot, consuming the draft. */
    public Template publish(String id) {
        return publish(id, null);
    }

    public Template publish(String id, RequestOptions options) {
        return executor.request("POST", "/templates/" + Util.encodePath(id) + "/publish", null, false, null, options, Template.class);
    }
}
