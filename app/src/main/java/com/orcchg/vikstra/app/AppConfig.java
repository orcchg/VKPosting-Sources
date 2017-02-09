package com.orcchg.vikstra.app;

import android.support.annotation.IntDef;

import com.orcchg.vikstra.domain.util.DebugSake;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public enum AppConfig {
    INSTANCE;

    /* Dump */
    // ------------------------------------------
    public static final int SEND_DUMP_FILE = 0;
    public static final int SEND_DUMP_EMAIL = 1;
    public static final int SEND_DUMP_SHARE = 2;
    @IntDef({SEND_DUMP_FILE, SEND_DUMP_EMAIL, SEND_DUMP_SHARE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SendDumpVia {}

    private @SendDumpVia int sendDumpFilesVia = SEND_DUMP_SHARE;

    @SendDumpVia
    public int sendDumpFilesVia() {
        return sendDumpFilesVia;
    }

    /* Group */
    // ------------------------------------------
    private boolean isAllGroupsSelected = true;
    private boolean isAllGroupsSortedByMembersCount = true;

    private @DebugSake boolean showSettingsMenuOnGroupListScreen = true;  // for DEBUG
    private boolean useInteractiveReportScreen = true;  // show ReportScreen instead of StatusDialog while posting
    private boolean useTutorialShowcases = true;

    public boolean isAllGroupsSelected() {
        return isAllGroupsSelected;
    }
    public boolean isAllGroupsSortedByMembersCount() {
        return isAllGroupsSortedByMembersCount;
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
