package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Post {

    static Builder builder() {
        return new AutoValue_Post.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Post build();
    }
}
