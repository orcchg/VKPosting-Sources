package com.orcchg.vikstra.domain;

public enum DomainConfig {
    INSTANCE;

    public int multiUseCaseSleepInterval() {
        return BuildConfig.multiUseCaseSleepInterval;
    }
    public int limitItemsPerRequest() {
        return BuildConfig.limitItemsPerRequest;
    }
    public int loadMoreThreshold() {
        return BuildConfig.loadMoreThreshold;
    }

    /* Group */
    // ------------------------------------------
    public boolean useOnlyGroupsWhereCanPostFreely() {
        return BuildConfig.useOnlyGroupsWhereCanPostFreely;
    }

    /* Log */
    // ------------------------------------------
    @Override
    public String toString() {
        return new StringBuilder("DomainConfig: ")
                .append("multiUseCaseSleepInterval=").append(multiUseCaseSleepInterval())
                .append(", limitItemsPerRequest=").append(limitItemsPerRequest())
                .append(", loadMoreThreshold=").append(loadMoreThreshold())
                .append(", useOnlyGroupsWhereCanPostFreely=").append(useOnlyGroupsWhereCanPostFreely())
                .toString();
    }
}
