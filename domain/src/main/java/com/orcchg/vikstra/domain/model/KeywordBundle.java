package com.orcchg.vikstra.domain.model;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class KeywordBundle implements Parcelable {

    public static Builder builder() {
        return new AutoValue_KeywordBundle.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setTitle(String title);
        public abstract Builder setKeywords(List<Keyword> keywords);
        public abstract KeywordBundle build();
    }

    public abstract long id();
    public abstract String title();
    public abstract List<Keyword> keywords();
}
