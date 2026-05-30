package com.anypost;

import com.anypost.model.WhoamiResponse;

/** The {@code /whoami} operation. */
public final class IdentityService {

    private final RequestExecutor executor;

    IdentityService(RequestExecutor executor) {
        this.executor = executor;
    }

    /** Identifies the team and permission level behind the current API key. */
    public WhoamiResponse whoami() {
        return whoami(null);
    }

    public WhoamiResponse whoami(RequestOptions options) {
        return executor.request("GET", "/whoami", null, false, null, options, WhoamiResponse.class);
    }
}
