package com.orcchg.vikstra.app.injection.module;

import android.content.Context;

import com.orcchg.vikstra.app.SharedPrefsManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SharedPrefsManagerModule {

    private final Context context;

    public SharedPrefsManagerModule(Context context) {
        this.context = context;
    }

    @Provides @Singleton
    SharedPrefsManager provideSharedPrefsManager() {
        return new SharedPrefsManager(context);
    }
}
