package com.orcchg.vikstra.domain.model;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Keyword implements Comparable<Keyword>, Parcelable {

    public static Keyword create(String keyword) {
        return new AutoValue_Keyword(keyword);
    }

    public abstract String keyword();

    @Override
    public int compareTo(Keyword o) {
        return keyword().compareTo(o.keyword());
    }
}
