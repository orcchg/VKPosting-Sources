package com.orcchg.vikstra.app.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.orcchg.vikstra.app.AndroidApplication;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.injection.component.DaggerNavigationComponent;
import com.orcchg.vikstra.app.injection.component.NavigationComponent;
import com.orcchg.vikstra.app.navigation.NavigatorHolder;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public abstract class BaseDialogFragment<V extends MvpView, P extends MvpPresenter<V>>
        extends DialogFragment implements MvpView {

    protected P presenter;
    protected NavigationComponent navigationComponent;

    private NavigatorHolder navigatorHolder = new NavigatorHolder();

    private boolean isStateRestored = false;

    @NonNull
    protected abstract P createPresenter();

    protected abstract void injectDependencies();

    @DebugLog @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(getClass().getSimpleName());
        Timber.i("onCreate(dialog fragment=%s)", hashCode());
        isStateRestored = savedInstanceState != null;
        injectNavigator();
        injectDependencies();
        presenter = createPresenter();
        presenter.attachView((V) this);
        presenter.onCreate(savedInstanceState);
    }

    @DebugLog @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.tag(getClass().getSimpleName());
        Timber.i("onActivityResult(dialog fragment=%s)", hashCode());
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onStart(dialog fragment=%s)", hashCode());
        presenter.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onResume(dialog fragment=%s)", hashCode());
        presenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onPause(dialog fragment=%s)", hashCode());
        presenter.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.tag(getClass().getSimpleName());
        Timber.i("onSaveInstanceState(dialog fragment=%s)", hashCode());
        presenter.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onStop(dialog fragment=%s)", hashCode());
        presenter.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onDestroy(dialog fragment=%s)", hashCode());
        presenter.onDestroy();
        presenter.detachView();
        navigationComponent.navigator().onDestroy();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) getActivity().getApplication()).getApplicationComponent();
    }

    @DebugLog
    protected boolean isStateRestored() {
        return isStateRestored;
    }

    private void injectNavigator() {
        navigationComponent = DaggerNavigationComponent.create();
        navigationComponent.inject(navigatorHolder);
    }
}
