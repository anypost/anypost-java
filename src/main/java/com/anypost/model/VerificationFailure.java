package com.anypost.model;

/**
 * A stable failure category plus a human-readable message.
 *
 * @param code    a stable, switchable failure code
 * @param message a human-readable description with record names interpolated
 */
public record VerificationFailure(String code, String message) {}
