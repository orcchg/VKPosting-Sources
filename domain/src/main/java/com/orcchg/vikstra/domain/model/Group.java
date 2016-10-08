package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Group {

    private String name;

    public static Group create() {
        return new AutoValue_Group();
    }
}
