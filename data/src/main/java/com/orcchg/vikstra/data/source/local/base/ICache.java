package com.orcchg.vikstra.data.source.local.base;

public interface ICache {
    boolean isEmpty();
    boolean isExpired();
    void clear();
    int totalItems();
}
