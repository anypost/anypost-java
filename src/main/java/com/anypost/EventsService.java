package com.anypost;

import com.anypost.model.Event;

/**
 * Read access to the {@code /events} stream. List-only &mdash; events are not
 * addressable by id. Access it via {@link Anypost#events}.
 */
public final class EventsService {

    private final RequestExecutor executor;

    EventsService(RequestExecutor executor) {
        this.executor = executor;
    }

    /** One page of the team's events, newest-first. */
    public Page<Event> list() {
        return list(EventListParams.DEFAULT, null);
    }

    public Page<Event> list(EventListParams params) {
        return list(params, null);
    }

    public Page<Event> list(EventListParams params, RequestOptions options) {
        return fetchPage(params == null ? EventListParams.DEFAULT : params, options);
    }

    private Page<Event> fetchPage(EventListParams params, RequestOptions options) {
        Query query = new Query();
        params.applyTo(query);
        PageEnvelope<Event> envelope =
                executor.request("GET", "/events", null, false, query, options, Json.pageEnvelopeOf(Event.class));
        return new Page<>(envelope, cursor -> fetchPage(params.withAfter(cursor), options));
    }
}
