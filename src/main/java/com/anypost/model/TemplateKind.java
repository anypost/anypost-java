package com.anypost.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** A template's authoring format. Immutable once a template exists. */
public enum TemplateKind {
    HTML("html"),
    MARKDOWN("markdown");

    private final String value;

    TemplateKind(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static TemplateKind from(String value) {
        for (TemplateKind k : values()) {
            if (k.value.equals(value)) {
                return k;
            }
        }
        return null;
    }
}
