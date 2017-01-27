package com.orcchg.vikstra.app.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.orcchg.vikstra.app.AndroidApplication;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.injection.component.DaggerNavigationComponent;
import com.orcchg.vikstra.app.injection.component.NavigationComponent;
import com.orcchg.vikstra.app.navigation.NavigatorHolder;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public abstract class BaseFragment<V extends MvpView, P extends MvpPresenter<V>>
        extends Fragment implements MvpView {

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
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onCreate");
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
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onActivityResult");
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onStart");
        presenter.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onResume");
        presenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onPause");
        presenter.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onSaveInstanceState");
        presenter.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onStop");
        presenter.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.tag(this.getClass().getSimpleName());
        Timber.i("onDestroy");
        presenter.onDestroy();
        presenter.detachView();
    }

    /* Component */
    // --------------------------------------------------------------------------------------------
    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) getActivity().getApplication()).getApplicationComponent();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog
    protected boolean isStateRestored() {
        return isStateRestored;
    }

    private void injectNavigator() {
        navigationComponent = DaggerNavigationComponent.create();
        navigationComponent.inject(navigatorHolder);
    }
}
