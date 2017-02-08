package com.orcchg.vikstra.app;

import com.orcchg.vikstra.domain.util.DebugSake;

public enum AppConfig {
    INSTANCE;

    /* Group */
    // ------------------------------------------
    private boolean isAllGroupsSelected = true;
    private boolean isAllGroupsSortedByMembersCount = true;
    private boolean sendDumpFilesViaEmail = true;
    private @DebugSake boolean showSettingsMenuOnGroupListScreen = true;  // for DEBUG
    private boolean useInteractiveReportScreen = true;  // show ReportScreen instead of StatusDialog while posting
    private boolean useTutorialShowcases = true;

    public boolean isAllGroupsSelected() {
        return isAllGroupsSelected;
    }
    public boolean isAllGroupsSortedByMembersCount() {
        return isAllGroupsSortedByMembersCount;
    }
    public boolean sendDumpFilesViaEmail() {
        return sendDumpFilesViaEmail;
    }
    @DebugSake public boolean showSettingsMenuOnGroupListScreen() {
        return showSettingsMenuOnGroupListScreen;
    }
    public boolean useInteractiveReportScreen() {
        return useInteractiveReportScreen;
    }
    public boolean useTutorialShowcases() {
        return useTutorialShowcases;
    }

    /* Keyword */
    // ------------------------------------------
    private boolean interceptKeywordClickOnVH = false;

    public boolean shouldInterceptKeywordClickOnVH() {
        return interceptKeywordClickOnVH;
    }

    /* Log */
    // ------------------------------------------
    @Override
    public String toString() {
        return new StringBuilder("AppConfig: ")
                .append("isAllGroupsSelected=").append(isAllGroupsSelected)
                .append(", isAllGroupsSortedByMembersCount=").append(isAllGroupsSortedByMembersCount)
                .append(", showSettingsMenuOnGroupListScreen=").append(showSettingsMenuOnGroupListScreen)
                .append(", useInteractiveReportScreen=").append(useInteractiveReportScreen)
                .append(", interceptKeywordClickOnVH=").append(interceptKeywordClickOnVH)
                .toString();
    }
}
