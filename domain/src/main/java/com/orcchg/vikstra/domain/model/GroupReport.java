package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class GroupReport {

    public static Builder builder() {
        return new AutoValue_GroupReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setWallPostId(int id);
        public abstract GroupReport build();
    }

    public abstract int wallPostId();
}
