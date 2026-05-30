package com.anypost.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A single message to send.
 *
 * <p>For a standalone send, {@code from} and {@code to} are required, and at least
 * one of {@code text}, {@code html}, or {@code templateId} must be set (the API
 * enforces this). As a batch entry, {@code from} (and any other shared field) may
 * be omitted to inherit the batch {@link EmailBatchRequest} defaults.
 *
 * <p>Build one with {@link #builder()}.
 */
public record SendEmailRequest(
        String from,
        List<String> to,
        List<String> cc,
        List<String> bcc,
        List<String> replyTo,
        String subject,
        String text,
        String html,
        String templateId,
        Map<String, String> headers,
        List<Attachment> attachments,
        List<String> tags,
        String campaign,
        String topic,
        Tracking tracking,
        Map<String, Object> variables,
        Unsubscribe unsubscribe) {

    public static Builder builder() {
        return new Builder();
    }

    /** A mutable builder for {@link SendEmailRequest}. */
    public static final class Builder {
        private String from;
        private List<String> to;
        private List<String> cc;
        private List<String> bcc;
        private List<String> replyTo;
        private String subject;
        private String text;
        private String html;
        private String templateId;
        private Map<String, String> headers;
        private List<Attachment> attachments;
        private List<String> tags;
        private String campaign;
        private String topic;
        private Tracking tracking;
        private Map<String, Object> variables;
        private Unsubscribe unsubscribe;

        /** The sender address, bare or {@code "Display Name <addr@host>"}. */
        public Builder from(String from) {
            this.from = from;
            return this;
        }

        /** The primary recipients (1&ndash;50). Combined to + cc + bcc must be &le; 50. */
        public Builder to(String... to) {
            this.to = new ArrayList<>(List.of(to));
            return this;
        }

        public Builder to(List<String> to) {
            this.to = to;
            return this;
        }

        public Builder cc(String... cc) {
            this.cc = new ArrayList<>(List.of(cc));
            return this;
        }

        public Builder cc(List<String> cc) {
            this.cc = cc;
            return this;
        }

        public Builder bcc(String... bcc) {
            this.bcc = new ArrayList<>(List.of(bcc));
            return this;
        }

        public Builder bcc(List<String> bcc) {
            this.bcc = bcc;
            return this;
        }

        /** One reply-to address, or up to 10. */
        public Builder replyTo(String... replyTo) {
            this.replyTo = new ArrayList<>(List.of(replyTo));
            return this;
        }

        public Builder replyTo(List<String> replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder html(String html) {
            this.html = html;
            return this;
        }

        /** A published template id ({@code template_<uuid>}). Excludes inline text/html. */
        public Builder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        /** Add a single custom header. */
        public Builder header(String name, String value) {
            if (this.headers == null) {
                this.headers = new LinkedHashMap<>();
            }
            this.headers.put(name, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        /** Add a single attachment. */
        public Builder attachment(Attachment attachment) {
            if (this.attachments == null) {
                this.attachments = new ArrayList<>();
            }
            this.attachments.add(attachment);
            return this;
        }

        public Builder attachments(List<Attachment> attachments) {
            this.attachments = attachments;
            return this;
        }

        /** Up to 10 free-form labels. */
        public Builder tags(String... tags) {
            this.tags = new ArrayList<>(List.of(tags));
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder campaign(String campaign) {
            this.campaign = campaign;
            return this;
        }

        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder tracking(Tracking tracking) {
            this.tracking = tracking;
            return this;
        }

        /** Add a single template variable. */
        public Builder variable(String name, Object value) {
            if (this.variables == null) {
                this.variables = new LinkedHashMap<>();
            }
            this.variables.put(name, value);
            return this;
        }

        public Builder variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        public Builder unsubscribe(Unsubscribe unsubscribe) {
            this.unsubscribe = unsubscribe;
            return this;
        }

        public SendEmailRequest build() {
            return new SendEmailRequest(from, to, cc, bcc, replyTo, subject, text, html,
                    templateId, headers, attachments, tags, campaign, topic, tracking,
                    variables, unsubscribe);
        }
    }
}
