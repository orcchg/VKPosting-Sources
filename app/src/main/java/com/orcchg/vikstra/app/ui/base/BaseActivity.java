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
import com.orcchg.vikstra.app.ui.util.UiUtility;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public abstract class BaseActivity<V extends MvpView, P extends MvpPresenter<V>>
        extends AppCompatActivity implements MvpView {

    protected P presenter;
    protected NavigationComponent navigationComponent;
    protected PermissionManagerComponent permissionManagerComponent;
    protected SharedPrefsManagerComponent sharedPrefsManagerComponent;

    private NavigatorHolder navigatorHolder = new NavigatorHolder();

    protected boolean isDestroying = false;

    public boolean isDestroying() {
        return isDestroying;
    }

    @NonNull
    protected abstract P createPresenter();

    protected abstract void injectDependencies();

    @DebugLog @Override @SuppressWarnings("unchecked")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        isDestroying = false;
        super.onCreate(savedInstanceState);
        Timber.tag(getClass().getSimpleName());
        Timber.i("onCreate, smallest width: %s", UiUtility.getSmallestWidth(this));
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
        Timber.i("onActivityResult");
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onStart");
        presenter.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onResume");
        presenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onPause");
        presenter.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.tag(getClass().getSimpleName());
        Timber.i("onSaveInstanceState");
        presenter.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onStop");
        presenter.onStop();
    }

    @Override
    protected void onDestroy() {
        isDestroying = true;
        super.onDestroy();
        Timber.tag(getClass().getSimpleName());
        Timber.i("onDestroy");
        presenter.onDestroy();
        presenter.detachView();
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
