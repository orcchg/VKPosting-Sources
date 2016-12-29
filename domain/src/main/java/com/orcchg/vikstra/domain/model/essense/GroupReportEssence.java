package com.orcchg.vikstra.domain.model.essense;

import com.google.auto.value.AutoValue;
import com.orcchg.vikstra.domain.model.Group;

@AutoValue
public abstract class GroupReportEssence implements Essence {

    public static Builder builder() {
        return new AutoValue_GroupReportEssence.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setErrorCode(int errorCode);
        public abstract Builder setGroup(Group group);
        public abstract Builder setWallPostId(long wallPostId);
        public abstract GroupReportEssence build();
    }

    public abstract int errorCode();
    public abstract Group group();
    public abstract long wallPostId();
}
