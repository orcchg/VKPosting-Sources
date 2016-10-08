package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Post {

    public static Post create() {
        return new AutoValue_Post();
    }
}
