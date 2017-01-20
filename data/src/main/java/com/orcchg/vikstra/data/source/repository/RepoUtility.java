package com.orcchg.vikstra.data.source.repository;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import timber.log.Timber;

public class RepoUtility {
    public static final int SOURCE_REMOTE = 0;
    public static final int SOURCE_LOCAL = 1;
    @IntDef({SOURCE_REMOTE, SOURCE_LOCAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SourceType {}

    public static void checkLimitAndOffset(int limit, int offset) {
        if (limit < 0 && offset != 0) {
            Timber.wtf("Negative limit is specified to fetch all items, offset must be equal to zero! Actual offset: %s", offset);
            throw new IllegalArgumentException("Wrong offset value, must be 0, when limit is negative!");
        }
    }

    public static void checkListBounds(int boundary, int total) {
        if (boundary >= total) {
            Timber.wtf("Boundary (offset + limit) %s exceeds total items count %s", boundary, total);
            throw new ArrayIndexOutOfBoundsException(boundary);
        }
    }
}
