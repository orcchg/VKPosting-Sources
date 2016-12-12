package com.orcchg.vikstra.app;

public enum AppConfig {
    INSTANCE;

    private boolean isAllGroupsSelected = true;
    private boolean isAllGroupsSortedByMembersCount = true;

    public boolean isAllGroupsSelected() {
        return isAllGroupsSelected;
    }
    public boolean isAllGroupsSortedByMembersCount() {
        return isAllGroupsSortedByMembersCount;
    }
}
