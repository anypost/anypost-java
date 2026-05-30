package com.anypost.model;

/**
 * A reusable email template. The {@code subject}/{@code html}/{@code text}/
 * {@code markdown} fields hold the published content and are {@code null} until
 * first published. Edits land in a draft; publishing promotes the draft. Sends
 * always use the published content.
 *
 * @param id          the {@code template_}-prefixed id
 * @param name        the identifier, unique within the team
 * @param subject     the published subject line, {@code null} until first published
 * @param kind        the authoring format
 * @param html        the published HTML body, {@code null} until first published
 * @param text        the published, machine-derived plain-text body, {@code null} until first published
 * @param markdown    the published emailmd source, set only for {@code kind=markdown}
 * @param hasDraft    whether an unpublished draft is pending
 * @param publishedAt when last published, or {@code null} if never
 * @param createdAt   the creation timestamp
 * @param updatedAt   the last-modified timestamp
 */
public record Template(
        String id,
        String name,
        String subject,
        TemplateKind kind,
        String html,
        String text,
        String markdown,
        boolean hasDraft,
        String publishedAt,
        String createdAt,
        String updatedAt) {}
