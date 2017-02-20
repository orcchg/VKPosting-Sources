package com.orcchg.vikstra.app.ui.base;

import android.app.IntentService;
import android.content.Intent;

import com.orcchg.vikstra.app.ui.util.MainLooperSpy;
import com.orcchg.vikstra.domain.util.DebugSake;

import timber.log.Timber;

public abstract class BaseIntentService extends IntentService {

    protected final @DebugSake MainLooperSpy mainLooperSpy = new MainLooperSpy();

    private boolean startedHandle = false;

    protected boolean wasStartedHandle() {
        return startedHandle;
    }

    public BaseIntentService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onCreate(intent service=%s)", hashCode());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.tag(getClass().getSimpleName());
        Timber.i("onHandleIntent(intent service=%s)", hashCode());
        startedHandle = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onDestroy(intent service=%s)", hashCode());
    }
}
