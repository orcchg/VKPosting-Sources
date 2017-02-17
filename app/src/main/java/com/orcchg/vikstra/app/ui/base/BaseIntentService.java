package com.orcchg.vikstra.app.ui.base;

import android.app.IntentService;
import android.content.Intent;

import timber.log.Timber;

public abstract class BaseIntentService extends IntentService {

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
        Timber.i("onCreate");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.tag(getClass().getSimpleName());
        Timber.i("onHandleIntent");
        startedHandle = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onDestroy");
    }
}
