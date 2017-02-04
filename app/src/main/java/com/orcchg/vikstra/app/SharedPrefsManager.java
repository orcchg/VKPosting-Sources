package com.orcchg.vikstra.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.orcchg.vikstra.domain.util.Constant;

import javax.inject.Inject;

public class SharedPrefsManager {
    private static final String SHARED_PREFS_FILE_NAME = "vikstra_shared_preferences";

    private final SharedPreferences sharedPreferences;

    @Inject
    public SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    /* Read & Write shared preferences */
    // --------------------------------------------------------------------------------------------
    /* Media */
    // ------------------------------------------
    private static final String MEDIA_UNIQUE_ID = "media_unique_id";

    public long getUniqueMediaId() {
        long id = sharedPreferences.getLong(MEDIA_UNIQUE_ID, Constant.INIT_ID);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(MEDIA_UNIQUE_ID, id + 1);
        editor.apply();
        return id;
    }
}
