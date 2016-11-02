package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Group {

    static Builder builder() {
        return new AutoValue_Group.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder setName(String name);
        abstract Group build();
    }

    abstract String name();
}
