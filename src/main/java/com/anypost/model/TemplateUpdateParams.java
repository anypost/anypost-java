package com.anypost.model;

/**
 * The body for updating a template. Only the name is mutable; content is
 * draft-versioned.
 *
 * @param name the new name
 */
public record TemplateUpdateParams(String name) {

    public static TemplateUpdateParams of(String name) {
        return new TemplateUpdateParams(name);
    }
}
