package com.anypost;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * One page of a list result, mirroring the wire envelope ({@link #data()},
 * {@link #hasMore()}, {@link #nextCursor()}).
 *
 * <p>Read one page, call {@link #next()} to fetch the following one, or iterate
 * {@link #all()} / {@link #stream()} to walk every item across pages, re-fetching
 * as it goes. A fetch failure during iteration throws {@link AnypostException}.
 *
 * @param <T> the item type
 */
public final class Page<T> {

    private final List<T> data;
    private final boolean hasMore;
    private final String nextCursor;
    private final Function<String, Page<T>> fetcher;

    Page(PageEnvelope<T> envelope, Function<String, Page<T>> fetcher) {
        this.data = envelope.data() == null ? Collections.emptyList() : envelope.data();
        this.hasMore = envelope.hasMore();
        this.nextCursor = envelope.nextCursor();
        this.fetcher = fetcher;
    }

    /** This page's items. */
    public List<T> data() {
        return data;
    }

    /** Whether another page exists. */
    public boolean hasMore() {
        return hasMore;
    }

    /** The cursor for the next page, or {@code null} when there are none. */
    public String nextCursor() {
        return nextCursor;
    }

    /** Fetches the following page, or returns {@code null} when there are none. */
    public Page<T> next() {
        if (!hasMore || nextCursor == null || nextCursor.isEmpty()) {
            return null;
        }
        return fetcher.apply(nextCursor);
    }

    /** Every item across this and all following pages, re-fetching lazily as it goes. */
    public Iterable<T> all() {
        return () -> new Iterator<>() {
            private Page<T> page = Page.this;
            private Iterator<T> current = Page.this.data.iterator();

            @Override
            public boolean hasNext() {
                while (!current.hasNext()) {
                    Page<T> nextPage = page.next();
                    if (nextPage == null) {
                        return false;
                    }
                    page = nextPage;
                    current = nextPage.data.iterator();
                }
                return true;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return current.next();
            }
        };
    }

    /** Every item across all pages, as a sequential stream. */
    public Stream<T> stream() {
        return StreamSupport.stream(all().spliterator(), false);
    }
}
