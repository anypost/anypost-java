package com.anypost;

import java.util.List;

/** The wire shape every list endpoint returns: {@code {data, has_more, next_cursor}}. */
record PageEnvelope<T>(List<T> data, boolean hasMore, String nextCursor) {}
