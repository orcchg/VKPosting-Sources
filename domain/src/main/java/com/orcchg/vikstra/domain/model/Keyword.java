package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Keyword {

    private String keyword;

    public static Keyword create() {
        return new AutoValue_Keyword();
    }
}
