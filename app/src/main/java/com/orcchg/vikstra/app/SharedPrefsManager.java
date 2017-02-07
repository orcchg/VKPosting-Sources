package com.orcchg.vikstra.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.orcchg.vikstra.app.ui.common.showcase.SingleShot;
import com.orcchg.vikstra.domain.util.Constant;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class SharedPrefsManager {
    private static final String SHARED_PREFS_FILE_NAME = "vikstra_shared_preferences";

    private final SharedPreferences sharedPreferences;

    @DebugLog @Inject
    public SharedPrefsManager(Context context) {
        Timber.d("SharedPrefsManager ctor");
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    /* Read & Write shared preferences */
    // --------------------------------------------------------------------------------------------
    /* Media */
    // ------------------------------------------
    private static final String MEDIA_UNIQUE_ID = "media_unique_id";

    @DebugLog
    public long getUniqueMediaId() {
        long id = sharedPreferences.getLong(MEDIA_UNIQUE_ID, Constant.INIT_ID);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(MEDIA_UNIQUE_ID, id + 1);
        editor.apply();
        return id;
    }

    /* Showcase */
    // ------------------------------------------
    @DebugLog
    public boolean checkShowcaseSingleShot(@SingleShot.ShowCase int showcase, @SingleShot.Screen int screen) {
        return sharedPreferences.getBoolean(makeSingleShotKey(showcase, screen), false);
    }

    @DebugLog
    public void notifyShowcaseFired(@SingleShot.ShowCase int showcase, @SingleShot.Screen int screen) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(makeSingleShotKey(showcase, screen), true);
        editor.apply();
    }

    private String makeSingleShotKey(@SingleShot.ShowCase int showcase, @SingleShot.Screen int screen) {
        return new StringBuilder("key_single_shot_").append(showcase).append('_').append(screen).toString();
    }
}
