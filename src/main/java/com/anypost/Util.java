package com.anypost;

/** Small internal helpers shared across the client. */
final class Util {

    private static final String UNRESERVED =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~";

    private Util() {}

    /**
     * Percent-encodes a single path segment, escaping everything outside the RFC
     * 3986 unreserved set (so {@code @}, {@code /}, {@code *}, and the like are
     * encoded). Matches the path encoding of the other Anypost SDKs.
     */
    static String encodePath(String segment) {
        StringBuilder b = new StringBuilder(segment.length());
        byte[] bytes = segment.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        for (byte value : bytes) {
            char c = (char) (value & 0xff);
            if (UNRESERVED.indexOf(c) >= 0) {
                b.append(c);
            } else {
                b.append('%').append(String.format("%02X", value & 0xff));
            }
        }
        return b.toString();
    }
}
