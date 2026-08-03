package com.anypost.model;

/**
 * The sending limits currently enforced against a team.
 *
 * <p>These are effective values and can differ from the plan defaults, so read
 * them at runtime rather than hardcoding them.
 *
 * @param daily                 messages the team may send per calendar day (UTC); exceeding it
 *                              returns 429 with scope {@code daily}
 * @param monthly               messages the team may send per billing month; exceeding it returns
 *                              429 with scope {@code monthly}, unless prepaid overage credits cover
 *                              the excess, which are not counted here
 * @param deliveryRatePerMinute how fast accepted mail is released to receiving servers. Not a
 *                              request limit and never a rejection: mail beyond this rate queues
 *                              and drains at the metered rate.
 */
public record WhoamiLimits(int daily, int monthly, int deliveryRatePerMinute) {}
