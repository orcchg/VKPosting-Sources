package com.orcchg.vikstra.domain.model.essense;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class GroupReportEssence implements Essence {

    public static Builder builder() {
        return new AutoValue_GroupReportEssence.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setErrorCode(int errorCode);
        public abstract Builder setGroupId(long id);
        public abstract Builder setWallPostId(int id);
        public abstract GroupReportEssence build();
    }

    public abstract int errorCode();
    public abstract long groupId();
    public abstract int wallPostId();
}
