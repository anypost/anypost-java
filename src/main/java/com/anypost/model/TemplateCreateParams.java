package com.anypost.model;

/**
 * The body for creating a template. The new template starts unpublished. For
 * {@code kind=html} supply {@code html}; for {@code kind=markdown} supply
 * {@code markdown}. The plain-text body is always derived server-side.
 */
public record TemplateCreateParams(
        String name,
        String subject,
        TemplateKind kind,
        String html,
        String markdown) {

    public static Builder builder() {
        return new Builder();
    }

    /** A mutable builder for {@link TemplateCreateParams}. */
    public static final class Builder {
        private String name;
        private String subject;
        private TemplateKind kind;
        private String html;
        private String markdown;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        /** Defaults to {@code html} server-side; immutable once the template exists. */
        public Builder kind(TemplateKind kind) {
            this.kind = kind;
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

        public TemplateCreateParams build() {
            return new TemplateCreateParams(name, subject, kind, html, markdown);
        }
    }
}
