package com.orcchg.vikstra.app.injection.module;

import android.content.Context;

import com.orcchg.vikstra.app.PermissionManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PermissionManagerModule {

    private final Context context;

    public PermissionManagerModule(Context context) {
        this.context = context;
    }

    @Provides @Singleton
    PermissionManager providePermissionManager() {
        return new PermissionManager(context);
    }
}
