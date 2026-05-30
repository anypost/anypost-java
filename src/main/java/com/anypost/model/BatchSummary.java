package com.anypost.model;

/** Tallies a batch's per-entry outcomes. */
public record BatchSummary(int total, int queued, int failed) {}
