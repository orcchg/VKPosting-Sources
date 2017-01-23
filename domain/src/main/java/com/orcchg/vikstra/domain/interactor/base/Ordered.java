package com.orcchg.vikstra.domain.interactor.base;

public class Ordered<Result> implements Comparable<Ordered<Result>> {
    public int orderId;
    public Result data;
    public Throwable error;
    public volatile boolean cancelled;

    @Override
    public int compareTo(Ordered<Result> o) {
        return orderId - o.orderId;
    }
}
