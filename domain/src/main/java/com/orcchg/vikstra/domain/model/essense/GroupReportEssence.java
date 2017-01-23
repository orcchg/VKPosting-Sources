package com.orcchg.vikstra.domain.model.essense;

import com.google.auto.value.AutoValue;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupReport;

@AutoValue
public abstract class GroupReportEssence implements Essence {

    public static Builder builder() {
        return new AutoValue_GroupReportEssence.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setCancelled(boolean cancelled);
        public abstract Builder setErrorCode(int errorCode);
        public abstract Builder setGroup(Group group);
        public abstract Builder setWallPostId(long wallPostId);
        public abstract GroupReportEssence build();
    }

    public abstract boolean cancelled();
    public abstract int errorCode();
    public abstract Group group();
    public abstract long wallPostId();

    @GroupReport.Status
    public int status() {
        if (cancelled()) return GroupReport.STATUS_CANCEL;
        if (errorCode() == 0) return GroupReport.STATUS_SUCCESS;
        return GroupReport.STATUS_FAILURE;
    }
}
