package com.anypost.model;

/** The inner error on a failed batch entry. */
public record BatchItemError(String type, String message) {}
