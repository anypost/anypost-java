package com.anypost.model;

/**
 * Bot classification for a proxied open or click. Pure-noise machine traffic
 * (mailbox prefetchers, scanners) never becomes an event, so the only kind a
 * customer ever sees is {@code proxy} — a real open whose origin is anonymized
 * by a mailbox image proxy (Gmail, Yahoo, etc.).
 *
 * @param source the detected mailbox image proxy, e.g. {@code google}, {@code yahoo}, {@code bing}
 * @param kind   always {@code proxy} on customer-visible events
 */
public record EventBot(String source, String kind) {}
