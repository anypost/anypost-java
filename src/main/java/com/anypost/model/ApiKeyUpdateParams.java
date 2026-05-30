package com.anypost.model;

import java.util.ArrayList;
import java.util.List;

/** The body for updating an API key. The plaintext secret is not rotated here. */
public record ApiKeyUpdateParams(
        String name,
        Permissions permissions,
        List<String> allowedDomains,
        List<String> allowedIps) {

    public static Builder builder() {
        return new Builder();
    }

    /** A mutable builder for {@link ApiKeyUpdateParams}. */
    public static final class Builder {
        private String name;
        private Permissions permissions;
        private List<String> allowedDomains;
        private List<String> allowedIps;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder permissions(Permissions permissions) {
            this.permissions = permissions;
            return this;
        }

        public Builder allowedDomains(String... allowedDomains) {
            this.allowedDomains = new ArrayList<>(List.of(allowedDomains));
            return this;
        }

        public Builder allowedDomains(List<String> allowedDomains) {
            this.allowedDomains = allowedDomains;
            return this;
        }

        public Builder allowedIps(String... allowedIps) {
            this.allowedIps = new ArrayList<>(List.of(allowedIps));
            return this;
        }

        public Builder allowedIps(List<String> allowedIps) {
            this.allowedIps = allowedIps;
            return this;
        }

        public ApiKeyUpdateParams build() {
            return new ApiKeyUpdateParams(name, permissions, allowedDomains, allowedIps);
        }
    }
}
