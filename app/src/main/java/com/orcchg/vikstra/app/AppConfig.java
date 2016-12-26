package com.orcchg.vikstra.app;

public enum AppConfig {
    INSTANCE;

    /* Group */
    // ------------------------------------------
    private boolean isAllGroupsSelected = true;
    private boolean isAllGroupsSortedByMembersCount = true;
    private boolean useOnlyGroupsWhereCanPostFreely = true;

    public boolean isAllGroupsSelected() {
        return isAllGroupsSelected;
    }
    public boolean isAllGroupsSortedByMembersCount() {
        return isAllGroupsSortedByMembersCount;
    }
    public boolean useOnlyGroupsWhereCanPostFreely() { return useOnlyGroupsWhereCanPostFreely; }

    /* Keyword */
    // ------------------------------------------
    private boolean interceptKeywordClickOnVH = false;

    public boolean shouldInterceptKeywordClickOnVH() {
        return interceptKeywordClickOnVH;
    }
}
