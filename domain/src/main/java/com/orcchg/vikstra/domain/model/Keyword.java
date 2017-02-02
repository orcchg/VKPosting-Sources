package com.orcchg.vikstra.domain.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.orcchg.vikstra.domain.util.Constant;

@AutoValue
public abstract class Keyword implements Comparable<Keyword>, Parcelable {

    public static Keyword create(String keyword) {
        return new AutoValue_Keyword(keyword);
    }

    public static Keyword empty() {
        return Keyword.create(Constant.NO_KEYWORD);
    }

    public abstract String keyword();

    @Override
    public int compareTo(@NonNull Keyword o) {
        return keyword().compareTo(o.keyword());
    }
}
