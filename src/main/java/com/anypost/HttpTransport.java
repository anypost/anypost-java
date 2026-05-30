package com.anypost;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * The single round-trip seam under the retry loop. The default implementation
 * delegates to {@link java.net.http.HttpClient}; tests inject a fake to return
 * canned responses without a network.
 */
@FunctionalInterface
interface HttpTransport {
    HttpResponse<byte[]> send(HttpRequest request) throws IOException, InterruptedException;
}
