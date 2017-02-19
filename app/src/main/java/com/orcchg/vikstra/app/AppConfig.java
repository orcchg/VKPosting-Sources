package com.orcchg.vikstra.app;

import android.support.annotation.IntDef;

import com.orcchg.vikstra.BuildConfig;
import com.orcchg.vikstra.domain.util.DebugSake;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public enum AppConfig {
    INSTANCE;

    /* Dump */
    // ------------------------------------------
    public static final int SEND_DUMP_FILE = BuildConfig.SEND_DUMP_FILE;
    public static final int SEND_DUMP_EMAIL = BuildConfig.SEND_DUMP_EMAIL;
    public static final int SEND_DUMP_SHARE = BuildConfig.SEND_DUMP_SHARE;
    @IntDef({SEND_DUMP_FILE, SEND_DUMP_EMAIL, SEND_DUMP_SHARE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SendDumpVia {}

    @SendDumpVia @SuppressWarnings("ResourceType")
    public int sendDumpFilesVia() {
        return BuildConfig.sendDumpFilesVia;
    }

    /* Group */
    // ------------------------------------------
    public boolean isAllGroupsSelected() {
        return BuildConfig.isAllGroupsSelected;
    }
    public boolean isAllGroupsSortedByMembersCount() {
        return BuildConfig.isAllGroupsSortedByMembersCount;
    }
    @DebugSake
    public boolean showSettingsMenuOnGroupListScreen() {
        return BuildConfig.showSettingsMenuOnGroupListScreen;
    }
    public boolean useTutorialShowcases() {
        return BuildConfig.useTutorialShowcases;
    }

    /* Keyword */
    // ------------------------------------------
    public boolean shouldInterceptKeywordClickOnVH() {
        return BuildConfig.interceptKeywordClickOnVH;
    }

    /* Log */
    // ------------------------------------------
    @Override
    public String toString() {
        return new StringBuilder("AppConfig: ")
                .append("sendDumpFilesVia=").append(sendDumpFilesVia())
                .append(", isAllGroupsSelected=").append(isAllGroupsSelected())
                .append(", isAllGroupsSortedByMembersCount=").append(isAllGroupsSortedByMembersCount())
                .append(", showSettingsMenuOnGroupListScreen=").append(showSettingsMenuOnGroupListScreen())
                .append(", useTutorialShowcases=").append(useTutorialShowcases())
                .append(", interceptKeywordClickOnVH=").append(shouldInterceptKeywordClickOnVH())
                .toString();
    }
}
