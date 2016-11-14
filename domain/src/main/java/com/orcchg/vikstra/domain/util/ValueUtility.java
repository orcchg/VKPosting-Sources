package com.orcchg.vikstra.domain.util;

import java.util.Random;

public class ValueUtility {

    public static boolean isAllTrue(boolean[] flags) {
        for (boolean b : flags) if (!b) return false;
        return true;
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
}
