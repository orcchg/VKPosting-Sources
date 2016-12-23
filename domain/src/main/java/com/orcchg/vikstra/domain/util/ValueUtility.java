package com.orcchg.vikstra.domain.util;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    public static <T> int sizeOf(@Nullable List<T> items) {
        if (items == null) return 0;
        return items.size();
    }
}
