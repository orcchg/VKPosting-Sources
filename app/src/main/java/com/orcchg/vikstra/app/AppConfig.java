package com.orcchg.vikstra.app;

public enum AppConfig {
    INSTANCE;

    /* Group */
    // ------------------------------------------
    private boolean isAllGroupsSelected = true;
    private boolean isAllGroupsSortedByMembersCount = true;

    public boolean isAllGroupsSelected() {
        return isAllGroupsSelected;
    }
    public boolean isAllGroupsSortedByMembersCount() {
        return isAllGroupsSortedByMembersCount;
    }

    /* Keyword */
    // ------------------------------------------
    private boolean interceptKeywordClickOnVH = false;

    public boolean shouldInterceptKeywordClickOnVH() {
        return interceptKeywordClickOnVH;
    }
}
