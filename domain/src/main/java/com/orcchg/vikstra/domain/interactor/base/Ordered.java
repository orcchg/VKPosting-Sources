package com.orcchg.vikstra.domain.interactor.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Ordered<Result> implements Comparable<Ordered<Result>> {
    public int orderId;
    public Result data;
    public Throwable error;
    public volatile boolean cancelled;
    public @Nullable IParameters parameters;

    @Override
    public int compareTo(@NonNull Ordered<Result> o) {
        return orderId - o.orderId;
    }
}
