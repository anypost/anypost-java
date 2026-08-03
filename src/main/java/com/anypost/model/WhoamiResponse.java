package com.anypost.model;

/**
 * The identity resolved from the request's API key.
 *
 * @param team   the team the key belongs to, or {@code null} if it could not be resolved
 * @param apiKey the key on the request
 * @param limits the sending limits in force for the team, or {@code null} if it could not be
 *               resolved
 */
public record WhoamiResponse(WhoamiTeam team, WhoamiApiKey apiKey, WhoamiLimits limits) {}
