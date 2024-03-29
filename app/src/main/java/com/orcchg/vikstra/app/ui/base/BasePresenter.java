package com.orcchg.vikstra.app.ui.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.injection.component.DaggerSharedPrefsManagerComponent;
import com.orcchg.vikstra.app.injection.component.SharedPrefsManagerComponent;
import com.orcchg.vikstra.app.injection.module.SharedPrefsManagerModule;

import java.lang.ref.WeakReference;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public abstract class BasePresenter<V extends MvpView> implements MvpPresenter<V> {

    private WeakReference<V> viewRef;
    protected SharedPrefsManagerComponent sharedPrefsManagerComponent;

    private boolean isFresh = true;
    private boolean isStateRestored = false;
    private boolean wasOnActivityResultHappened = false;
    protected Bundle savedInstanceState;

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
    protected boolean wasOnActivityResultHappened() {
        return wasOnActivityResultHappened;
    }

    @DebugLog
    protected abstract void freshStart();  // called only at fresh start in onStart() lifecycle callback

    @DebugLog
    protected abstract void onRestoreState();  // called only at fresh start after state restoring in onStart()

    @DebugLog @Override
    public void detachView() {
        if (viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
    }

    @DebugLog @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Timber.tag(getClass().getSimpleName());
        Timber.i("onCreate(presenter=%s)", hashCode());
        injectSharedPrefsManager();
        isStateRestored = savedInstanceState != null;
        this.savedInstanceState = savedInstanceState;
        // to override
    }

    @DebugLog @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.tag(getClass().getSimpleName());
        Timber.i("onActivityResult(presenter=%s)", hashCode());
        wasOnActivityResultHappened = true;
        // to override
    }

    @Override
    public void onStart() {
        Timber.tag(getClass().getSimpleName());
        Timber.i("onStart(presenter=%s)", hashCode());
        if (isFresh) {
            /**
             * {@link BasePresenter#isStateRestored} flag is sticky - is doesn't get dropped when the
             * state of the screen has been restored after configuration change. But we must not call
             * initialization logic repeatedly in every {@link BasePresenter#onStart()}, we must do this
             * once at fresh start.
             */
            if (isStateRestored) {
                Timber.tag(getClass().getSimpleName());
                Timber.i("State restored on fresh start. (presenter=%s)", hashCode());
                onRestoreState();
            } else {
                Timber.tag(getClass().getSimpleName());
                Timber.i("Fresh start. (presenter=%s)", hashCode());
                freshStart();
            }
            isFresh = false;
        }
        // to override
    }

    @Override
    public void onResume() {
        Timber.tag(getClass().getSimpleName());
        Timber.i("onResume(presenter=%s)", hashCode());
        // to override
    }

    @Override
    public void onPause() {
        Timber.tag(getClass().getSimpleName());
        Timber.i("onPause(presenter=%s)", hashCode());
        // to override
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Timber.tag(getClass().getSimpleName());
        Timber.i("onSaveInstanceState(presenter=%s)", hashCode());
        // to override
    }

    @Override
    public void onStop() {
        Timber.tag(getClass().getSimpleName());
        Timber.i("onStop(presenter=%s)", hashCode());
        // to override
    }

    @Override
    public void onDestroy() {
        Timber.tag(getClass().getSimpleName());
        Timber.i("onDestroy(presenter=%s)", hashCode());
        // to override
    }

    /* Component */
    // --------------------------------------------------------------------------------------------
    @Nullable
    protected ApplicationComponent getApplicationComponent() {
        V view = getView();
        if (BaseActivity.class.isInstance(view)) {
            return ((BaseActivity) view).getApplicationComponent();
        }
        if (BaseFragment.class.isInstance(view)) {
            return ((BaseFragment) view).getApplicationComponent();
        }
        Timber.tag(getClass().getSimpleName());
        Timber.d("Application component is null - either view is not attached or it is not an instance of Base* class. (presenter=%s)", hashCode());
        return null;
    }

    public SharedPrefsManagerComponent getSharedPrefsManagerComponent() {
        return sharedPrefsManagerComponent;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private void injectSharedPrefsManager() {
        // app component isn't null, because onCreate() is followed by attachView()
        Context context = getApplicationComponent().context();
        sharedPrefsManagerComponent = DaggerSharedPrefsManagerComponent.builder()
            .sharedPrefsManagerModule(new SharedPrefsManagerModule(context))
            .build();
    }
}
