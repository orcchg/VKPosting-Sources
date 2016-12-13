package com.orcchg.vikstra.app.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.navigation.Navigator;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public abstract class BasePresenter<V extends MvpView> implements MvpPresenter<V> {

    private WeakReference<V> viewRef;
    protected @Inject Navigator navigator;

    private boolean isFresh = true;
    private boolean isStateRestored = false;

    @DebugLog @Override
    public void attachView(V view) {
        viewRef = new WeakReference<>(view);
    }

    @Nullable
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
        isStateRestored = savedInstanceState != null;
        // to override
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // to override
    }

    @DebugLog @Override
    public void onStart() {
        if (isFresh) {
            isFresh = false;
            freshStart();
        }
        // to override
    }

    @DebugLog @Override
    public void onResume() {
        // to override
    }

    @DebugLog @Override
    public void onPause() {
        // to override
    }

    @DebugLog @Override
    public void onSaveInstanceState(Bundle outState) {
        // to override
    }

    @DebugLog @Override
    public void onStop() {
        // to override
    }

    @DebugLog @Override
    public void onDestroy() {
        // to override
    }
}
