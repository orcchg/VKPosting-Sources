package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class GroupBundle {

    static Builder builder() {
        return new AutoValue_GroupBundle.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder setTitle(String title);
        abstract Builder setGroups(List<Group> groups);
        abstract Builder setKeywords(List<Keyword> keywords);
        abstract GroupBundle build();
    }

    abstract String title();
    abstract List<Group> groups();
    abstract List<Keyword> keywords();
}
