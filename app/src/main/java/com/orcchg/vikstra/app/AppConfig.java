package com.orcchg.vikstra.app;

import com.orcchg.vikstra.domain.util.DebugSake;

public enum AppConfig {
    INSTANCE;

    // TODO: list configuration on application start

    /* Group */
    // ------------------------------------------
    private boolean isAllGroupsSelected = true;
    private boolean isAllGroupsSortedByMembersCount = true;
    private @DebugSake boolean showSettingsMenuOnGroupListScreen = true;  // for DEBUG
    private boolean useInteractiveReportScreen = true;  // show ReportScreen instead of StatusDialog while posting
    private boolean useOnlyGroupsWhereCanPostFreely = true;

    public boolean isAllGroupsSelected() {
        return isAllGroupsSelected;
    }
    public boolean isAllGroupsSortedByMembersCount() {
        return isAllGroupsSortedByMembersCount;
    }
    @DebugSake public boolean showSettingsMenuOnGroupListScreen() {
        return showSettingsMenuOnGroupListScreen;
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
