package com.anypost;

/**
 * The cursor-pagination parameters shared by every list endpoint. {@link #DEFAULT}
 * requests the first page with the server default size.
 */
public final class ListParams {

    /** First page, server default page size. */
    public static final ListParams DEFAULT = builder().build();

    private final Integer limit;
    private final String after;

    private ListParams(Integer limit, String after) {
        this.limit = limit;
        this.after = after;
    }

    public static Builder builder() {
        return new Builder();
    }

    void applyTo(Query query) {
        query.set("limit", limit);
        query.set("after", after);
    }

    ListParams withAfter(String cursor) {
        return new ListParams(limit, cursor);
    }

    /** A mutable builder for {@link ListParams}. */
    public static final class Builder {
        private Integer limit;
        private String after;

        /** Page size, 1&ndash;100. Leave unset to use the server default (20). */
        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        /** A cursor from a previous page's {@code nextCursor}. Opaque &mdash; do not parse. */
        public Builder after(String after) {
            this.after = after;
            return this;
        }

        public ListParams build() {
            return new ListParams(limit, after);
        }
    }
}
