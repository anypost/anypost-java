package com.anypost.model;

/**
 * The body for creating a sending domain.
 *
 * @param name the domain to add, e.g. {@code example.com}
 */
public record DomainCreateParams(String name) {

    public static DomainCreateParams of(String name) {
        return new DomainCreateParams(name);
    }
}
