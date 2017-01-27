package com.orcchg.vikstra.domain;

public enum DomainConfig {
    INSTANCE;

    public final int multiUseCaseSleepInterval = 1000;  // to avoid Captcha error, interval in ms
    public final int limitItemsPerRequest = 20;
    public final int loadMoreThreshold = 1;

    /* Group */
    // ------------------------------------------
    private boolean useOnlyGroupsWhereCanPostFreely = true;

    public boolean useOnlyGroupsWhereCanPostFreely() { return useOnlyGroupsWhereCanPostFreely; }

    /* Log */
    // ------------------------------------------
    @Override
    public String toString() {
        return new StringBuilder("DomainConfig: ")
                .append("multiUseCaseSleepInterval=").append(multiUseCaseSleepInterval)
                .append(", limitItemsPerRequest=").append(limitItemsPerRequest)
                .append(", loadMoreThreshold=").append(loadMoreThreshold)
                .append(", useOnlyGroupsWhereCanPostFreely=").append(useOnlyGroupsWhereCanPostFreely)
                .toString();
    }
}
