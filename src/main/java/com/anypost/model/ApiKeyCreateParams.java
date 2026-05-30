package com.anypost.model;

import java.util.ArrayList;
import java.util.List;

/** The body for creating an API key. */
public record ApiKeyCreateParams(
        String name,
        Permissions permissions,
        List<String> allowedDomains,
        List<String> allowedIps) {

    public static Builder builder() {
        return new Builder();
    }

    /** A mutable builder for {@link ApiKeyCreateParams}. */
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

        /** Restrict sending to these domains. Omit for all verified domains. */
        public Builder allowedDomains(String... allowedDomains) {
            this.allowedDomains = new ArrayList<>(List.of(allowedDomains));
            return this;
        }

        public Builder allowedDomains(List<String> allowedDomains) {
            this.allowedDomains = allowedDomains;
            return this;
        }

        /** Restrict use to these IPs/CIDRs. Omit for all IPs. */
        public Builder allowedIps(String... allowedIps) {
            this.allowedIps = new ArrayList<>(List.of(allowedIps));
            return this;
        }

        public Builder allowedIps(List<String> allowedIps) {
            this.allowedIps = allowedIps;
            return this;
        }

        public ApiKeyCreateParams build() {
            return new ApiKeyCreateParams(name, permissions, allowedDomains, allowedIps);
        }
    }
}
