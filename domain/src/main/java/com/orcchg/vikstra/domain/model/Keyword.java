package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Keyword {

    public static Keyword create(String keyword) {
        return new AutoValue_Keyword(keyword);
    }

    abstract String keyword();
}
