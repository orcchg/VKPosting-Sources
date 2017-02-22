package com.orcchg.vikstra.app.ui.base;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.util.MainLooperSpy;
import com.orcchg.vikstra.domain.util.DebugSake;

import timber.log.Timber;

public abstract class BaseService extends Service {

    protected final @DebugSake MainLooperSpy mainLooperSpy = new MainLooperSpy();

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onCreate(service=%s)", hashCode());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onDestroy(service=%s)", hashCode());
    }
}
