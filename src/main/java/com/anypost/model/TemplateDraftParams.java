package com.anypost.model;

/**
 * The body for creating or updating a template's draft. For {@code kind=html}
 * supply {@code html}; for {@code kind=markdown} supply {@code markdown}.
 */
public record TemplateDraftParams(String subject, String html, String markdown) {

    public static Builder builder() {
        return new Builder();
    }

    /** A mutable builder for {@link TemplateDraftParams}. */
    public static final class Builder {
        private String subject;
        private String html;
        private String markdown;

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder html(String html) {
            this.html = html;
            return this;
        }

        public Builder markdown(String markdown) {
            this.markdown = markdown;
            return this;
        }

        public TemplateDraftParams build() {
            return new TemplateDraftParams(subject, html, markdown);
        }
    }
}
