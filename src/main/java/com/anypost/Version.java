package com.anypost;

/**
 * The SDK version, reported in the {@code User-Agent} header.
 *
 * <p>This is the single source of truth: bump it, set the same value as the
 * {@code <version>} in {@code pom.xml}, tag the commit {@code vX.Y.Z}, and push.
 * The release workflow publishes that tag to Maven Central.
 */
final class Version {
    static final String VERSION = "1.0.0";

    private Version() {}
}
