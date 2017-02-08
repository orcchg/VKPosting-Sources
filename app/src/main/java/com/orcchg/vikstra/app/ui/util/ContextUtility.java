package com.orcchg.vikstra.app.ui.util;

import android.app.Activity;
import android.os.Build;

import com.orcchg.vikstra.app.ui.base.BaseActivity;

import hugo.weaving.DebugLog;

public class ContextUtility {
    // TODO: move to shared prefs

    private static int sIncrementalValue = 1;

    public static String defaultTitle() {
        return "Default " + sIncrementalValue++;
    }

    @DebugLog
    public static boolean isActivityDestroyed(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return activity.isDestroyed();
        }
        if (BaseActivity.class.isInstance(activity)) {
            return ((BaseActivity) activity).isDestroying();
        }
        return activity.isFinishing();
    }
}
