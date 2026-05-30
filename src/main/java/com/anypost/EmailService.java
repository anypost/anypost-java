package com.anypost;

import com.anypost.model.BatchResponse;
import com.anypost.model.EmailBatchRequest;
import com.anypost.model.SendEmailRequest;
import com.anypost.model.SendResponse;

/** The {@code /email} operations. Access it via {@link Anypost#email}. */
public final class EmailService {

    private final RequestExecutor executor;

    EmailService(RequestExecutor executor) {
        this.executor = executor;
    }

    /**
     * Sends a single message. All addresses in to/cc/bcc share one envelope.
     *
     * <p>When retries are enabled and no idempotency key is supplied, the client
     * generates one so a retried send cannot deliver twice. Pass a
     * {@link RequestOptions#idempotencyKey(String)} to dedupe across process
     * restarts.
     */
    public SendResponse send(SendEmailRequest email) {
        return send(email, null);
    }

    public SendResponse send(SendEmailRequest email, RequestOptions options) {
        return executor.request("POST", "/email", email, true, null, options, SendResponse.class);
    }

    /**
     * Sends 1&ndash;100 independent messages in one request. A mixed-outcome batch
     * (HTTP 207) returns normally &mdash; inspect each entry's status in
     * {@link BatchResponse#data()}; it does not throw.
     */
    public BatchResponse sendBatch(EmailBatchRequest batch) {
        return sendBatch(batch, null);
    }

    public BatchResponse sendBatch(EmailBatchRequest batch, RequestOptions options) {
        return executor.request("POST", "/email/batch", batch, true, null, options, BatchResponse.class);
    }
}
