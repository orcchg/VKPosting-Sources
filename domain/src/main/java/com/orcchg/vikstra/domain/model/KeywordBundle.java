package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class KeywordBundle {

    static Builder builder() {
        return new AutoValue_KeywordBundle.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder setTitle(String title);
        abstract Builder setKeywords(List<Keyword> keywords);
        abstract KeywordBundle build();
    }

    abstract String title();
    abstract List<Keyword> keywords();
}
