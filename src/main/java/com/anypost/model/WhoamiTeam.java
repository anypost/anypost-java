package com.anypost.model;

/**
 * Identifies the team behind the API key.
 *
 * @param id   the team id
 * @param name the team name
 */
public record WhoamiTeam(String id, String name) {}
