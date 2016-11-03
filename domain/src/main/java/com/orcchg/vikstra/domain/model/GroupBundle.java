package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class GroupBundle {

    public static Builder builder() {
        return new AutoValue_GroupBundle.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setTitle(String title);
        public abstract Builder setGroups(List<Group> groups);
        public abstract Builder setKeywords(List<Keyword> keywords);
        public abstract GroupBundle build();
    }

    public abstract String title();
    public abstract List<Group> groups();
    public abstract List<Keyword> keywords();

    public KeywordBundle extractKeywordBundle() {
        return KeywordBundle.builder()
                .setTitle(title())
                .setKeywords(keywords())
                .build();
    }
}
