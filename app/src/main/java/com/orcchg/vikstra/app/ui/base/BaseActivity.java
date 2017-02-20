package com.orcchg.vikstra.app.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.orcchg.vikstra.app.AndroidApplication;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.injection.component.DaggerNavigationComponent;
import com.orcchg.vikstra.app.injection.component.DaggerPermissionManagerComponent;
import com.orcchg.vikstra.app.injection.component.DaggerSharedPrefsManagerComponent;
import com.orcchg.vikstra.app.injection.component.NavigationComponent;
import com.orcchg.vikstra.app.injection.component.PermissionManagerComponent;
import com.orcchg.vikstra.app.injection.component.SharedPrefsManagerComponent;
import com.orcchg.vikstra.app.injection.module.PermissionManagerModule;
import com.orcchg.vikstra.app.injection.module.SharedPrefsManagerModule;
import com.orcchg.vikstra.app.navigation.NavigatorHolder;
import com.orcchg.vikstra.app.ui.util.MainLooperSpy;
import com.orcchg.vikstra.app.ui.util.UiUtility;
import com.orcchg.vikstra.domain.util.DebugSake;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public abstract class BaseActivity<V extends MvpView, P extends MvpPresenter<V>>
        extends AppCompatActivity implements MvpView {

    protected final @DebugSake MainLooperSpy mainLooperSpy = new MainLooperSpy();

    protected P presenter;
    protected NavigationComponent navigationComponent;
    protected PermissionManagerComponent permissionManagerComponent;
    protected SharedPrefsManagerComponent sharedPrefsManagerComponent;

    private NavigatorHolder navigatorHolder = new NavigatorHolder();

    private boolean isStateRestored = false;
    protected boolean isDestroying = false;

    protected boolean isStateRestored() {
        return isStateRestored;
    }
    public boolean isDestroying() {
        return isDestroying;
    }

    @NonNull
    protected abstract P createPresenter();

    protected abstract void injectDependencies();

    @DebugLog @Override @SuppressWarnings("unchecked")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        isStateRestored = savedInstanceState != null;
        isDestroying = false;
        super.onCreate(savedInstanceState);
        Timber.tag(getClass().getSimpleName());
        Timber.i("onCreate(activity=%s), smallest width: %s", hashCode(), UiUtility.getSmallestWidth(this));
        injectNavigator();
        injectPermissionManager();
        injectSharedPrefsManager();
        injectDependencies();
        presenter = createPresenter();
        presenter.attachView((V) this);
        presenter.onCreate(savedInstanceState);
    }

    @DebugLog @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.tag(getClass().getSimpleName());
        Timber.i("onActivityResult(activity=%s)", hashCode());
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onStart(activity=%s)", hashCode());
        presenter.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onResume(activity=%s)", hashCode());
        presenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onPause(activity=%s)", hashCode());
        presenter.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.tag(getClass().getSimpleName());
        Timber.i("onSaveInstanceState(activity=%s)", hashCode());
        presenter.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onStop(activity=%s)", hashCode());
        presenter.onStop();
    }

    @Override
    protected void onDestroy() {
        isDestroying = true;
        super.onDestroy();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onDestroy(activity=%s)", hashCode());
        presenter.onDestroy();
        presenter.detachView();
        navigationComponent.navigator().onDestroy();
    }

    /* Component */
    // --------------------------------------------------------------------------------------------
    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) getApplication()).getApplicationComponent();
    }

    public NavigationComponent getNavigationComponent() {
        return navigationComponent;
    }

    public PermissionManagerComponent getPermissionManagerComponent() {
        return permissionManagerComponent;
    }

    public SharedPrefsManagerComponent getSharedPrefsManagerComponent() {
        return sharedPrefsManagerComponent;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private void injectPermissionManager() {
        permissionManagerComponent = DaggerPermissionManagerComponent.builder()
            .permissionManagerModule(new PermissionManagerModule(getApplicationContext()))
            .build();
    }

    private void injectNavigator() {
        navigationComponent = DaggerNavigationComponent.create();
        navigationComponent.inject(navigatorHolder);
    }

    private void injectSharedPrefsManager() {
        sharedPrefsManagerComponent = DaggerSharedPrefsManagerComponent.builder()
                .sharedPrefsManagerModule(new SharedPrefsManagerModule(this))
                .build();
    }
}
