package com.orcchg.vikstra.domain.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.interactor.base.Ordered;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class ValueUtility {

    public static boolean isAllTrue(boolean[] flags) {
        for (boolean b : flags) if (!b) return false;
        return true;
    }

    public static <Model> List<Model> merge(List<List<Model>> splitModels) {
        List<Model> list = new ArrayList<>();
        for (List<Model> models : splitModels) {
            list.addAll(models);
        }
        return list;
    }

    public static String time() {
        return time(System.currentTimeMillis());
    }

    public static String time(long millis) {
        long xdays    = TimeUnit.MILLISECONDS.toDays(millis);
        long xhours   = TimeUnit.MILLISECONDS.toHours(millis);
        long xminutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long xseconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        long days = xdays;
        long hours = xhours - TimeUnit.DAYS.toHours(xdays);
        long minutes = xminutes - TimeUnit.HOURS.toMinutes(xhours);
        long seconds = xseconds - TimeUnit.MINUTES.toSeconds(xminutes);
        return String.format("%02d %02d:%02d:%02d", days, hours, minutes, seconds);
    }

    public static long random(long min, long max) {
        if (min == max) {
            return min;
        }
        if (min > max) {
            throw new IllegalArgumentException("Min is greater than max!");
        }
        Random rng = new Random();
        return rng.nextLong() % (max - min) + min;
    }

    public static <T> boolean contains(@NonNull T item, T... items) {
        if (!isEmpty(items)) {
            for (int i = 0; i < items.length; ++i) {
                if (item.equals(items[i])) return true;
            }
        }
        return false;
    }

    @DebugLog
    public static boolean containsClass(@NonNull Object item, Class[] items) {
        Timber.v("Item class: %s", item.getClass().getSimpleName());
        if (!isEmpty(items)) {
            for (int i = 0; i < items.length; ++i) {
                if (items[i].isInstance(item)) return true;
            }
        }
        return false;
    }

    public static <T> boolean isEmpty(T... items) {
        return items == null || items.length == 0;
    }

    public static <T> int sizeOf(T... items) {
        if (items == null) return 0;
        return items.length;
    }

    public static <T> int sizeOf(@Nullable List<T> items) {
        if (items == null) return 0;
        return items.size();
    }

    /* Ordered result */
    // ------------------------------------------
    public static <Result> List<Result> unwrap(List<Ordered<Result>> results) {
        List<Result> list = new ArrayList<>();
        for (Ordered<Result> item : results) {
            if (item.data != null) list.add(item.data);
        }
        return list;
    }
}
