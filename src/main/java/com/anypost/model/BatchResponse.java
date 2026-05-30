package com.anypost.model;

import java.util.List;

/**
 * Returned from a batch send. A mixed-outcome batch (HTTP 207) is a success, not
 * an error: inspect each entry's {@code status}. {@code data.get(i).index() == i}.
 *
 * @param summary the queued/failed tallies
 * @param data    one result per entry, in request order
 */
public record BatchResponse(BatchSummary summary, List<BatchItemResult> data) {}
