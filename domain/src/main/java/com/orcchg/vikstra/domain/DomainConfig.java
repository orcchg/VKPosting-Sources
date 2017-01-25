package com.orcchg.vikstra.domain;

public enum DomainConfig {
    INSTANCE;

    // TODO: list configuration on application start

    public final int multiUseCaseSleepInterval = 1000;  // to avoid Captcha error, interval in ms
    public final int limitItemsPerRequest = 20;
    public final int loadMoreThreshold = 1;

    /* Group */
    // ------------------------------------------
    private boolean useOnlyGroupsWhereCanPostFreely = true;

    public boolean useOnlyGroupsWhereCanPostFreely() { return useOnlyGroupsWhereCanPostFreely; }
}
