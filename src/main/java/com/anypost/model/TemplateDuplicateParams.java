package com.anypost.model;

/**
 * The body for duplicating a template.
 *
 * @param name the name for the copy, or {@code null} to default to
 *             {@code "<source name> (copy)"}
 */
public record TemplateDuplicateParams(String name) {

    public static TemplateDuplicateParams of(String name) {
        return new TemplateDuplicateParams(name);
    }
}
