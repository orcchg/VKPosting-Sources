package com.orcchg.vikstra.domain.model;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Media implements Parcelable {

    public static Builder builder() {
        return new AutoValue_Media.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setUrl(String url);
        public abstract Media build();
    }

    public abstract long id();
    public abstract String url();
}
