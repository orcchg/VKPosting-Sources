package com.orcchg.vikstra.app.util;

import android.content.Context;

import com.orcchg.vikstra.R;

public class ViewUtility {

    private static boolean sEnabledImageTransition = false;

    public static boolean isLargeScreen(Context context) {
        return context.getResources().getBoolean(R.bool.isLargeScreen);
    }

    public static void enableImageTransition(boolean isEnabled) {
        sEnabledImageTransition = isEnabled;
    }

    public static boolean isImageTransitionEnabled() {
        return sEnabledImageTransition;
    }
}
