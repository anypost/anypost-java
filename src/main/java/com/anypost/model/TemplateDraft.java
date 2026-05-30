package com.anypost.model;

/**
 * The unpublished draft content for a template.
 *
 * @param subject   the draft subject line, or {@code null}
 * @param html      the draft HTML body, or {@code null}
 * @param text      the machine-derived plain-text body (always derived from html/markdown)
 * @param markdown  the draft emailmd source, or {@code null}
 * @param updatedAt the last-modified timestamp
 */
public record TemplateDraft(
        String subject,
        String html,
        String text,
        String markdown,
        String updatedAt) {}
