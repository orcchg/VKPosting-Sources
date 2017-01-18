package com.orcchg.vikstra.domain.test;

import timber.log.Timber;

public class TestUtil {

    public static void test(Object source, boolean value, String goodMessage, String badMessage) {
        if (value) {
            Timber.i("TEST[%s]: SUCCESS: %s", source.getClass().getSimpleName(), goodMessage);
        } else {
            Timber.e("TEST[%s]: FAILURE: %s", source.getClass().getSimpleName(), badMessage);
        }
    }
}
