package com.orcchg.vikstra.app;

public enum AppConfig {
    INSTANCE;

    // TODO: list configuration on application start

    /* Group */
    // ------------------------------------------
    private boolean isAllGroupsSelected = true;
    private boolean isAllGroupsSortedByMembersCount = true;
    private boolean useInteractiveReportScreen = true;  // show ReportScreen instead of StatusDialog while posting
    private boolean useOnlyGroupsWhereCanPostFreely = true;

    public boolean isAllGroupsSelected() {
        return isAllGroupsSelected;
    }
    public boolean isAllGroupsSortedByMembersCount() {
        return isAllGroupsSortedByMembersCount;
    }
    public boolean useInteractiveReportScreen() { return useInteractiveReportScreen; }
    public boolean useOnlyGroupsWhereCanPostFreely() { return useOnlyGroupsWhereCanPostFreely; }

    /* Keyword */
    // ------------------------------------------
    private boolean interceptKeywordClickOnVH = false;

    public boolean shouldInterceptKeywordClickOnVH() {
        return interceptKeywordClickOnVH;
    }
}
