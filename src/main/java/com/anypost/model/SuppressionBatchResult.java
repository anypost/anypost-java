package com.anypost.model;

import java.util.List;

/**
 * Returned from a suppressions batch. {@code imported + alreadySuppressed +
 * invalid.size()} equals the number of entries sent.
 *
 * @param imported          the suppressions this call created
 * @param alreadySuppressed entries whose address and topic were already suppressed,
 *                          including repeats within the batch; left unchanged
 * @param invalid           entries with an invalid address, in request order
 */
public record SuppressionBatchResult(int imported, int alreadySuppressed, List<SuppressionBatchInvalid> invalid) {}
