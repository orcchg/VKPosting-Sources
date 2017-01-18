package com.orcchg.vikstra.app.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.navigation.Navigator;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public abstract class BasePresenter<V extends MvpView> implements MvpPresenter<V> {

    private WeakReference<V> viewRef;
    protected @Inject Navigator navigator;

    private boolean isFresh = true;
    private boolean isStateRestored = false;

    @DebugLog @Override
    public void attachView(V view) {
        viewRef = new WeakReference<>(view);
    }

    //@Nullable
    public V getView() {
        return viewRef == null ? null : viewRef.get();
    }

    public boolean isViewAttached() {
        return viewRef != null && viewRef.get() != null;
    }

    protected boolean isOnFreshStart() {
        return isFresh;
    }

    protected boolean isStateRestored() {
        return isStateRestored;
    }

    @DebugLog
    protected abstract void freshStart();  // called only at fresh start in onStart() lifecycle callback

    @DebugLog @Override
    public void detachView() {
        if (viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
    }

    @DebugLog @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onCreate");
        isStateRestored = savedInstanceState != null;
        // to override
    }

    @DebugLog @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onActivityResult");
        // to override
    }

    @Override
    public void onStart() {
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onStart");
        if (isFresh) {
            Timber.tag(this.getClass().getSimpleName());
            Timber.i("Fresh start");
            isFresh = false;
            freshStart();
        }
        // to override
    }

    @Override
    public void onResume() {
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onResume");
        // to override
    }

    @Override
    public void onPause() {
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onPause");
        // to override
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onSaveInstanceState");
        // to override
    }

    @Override
    public void onStop() {
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onStop");
        // to override
    }

    @Override
    public void onDestroy() {
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onDestroy");
        // to override
    }
}
