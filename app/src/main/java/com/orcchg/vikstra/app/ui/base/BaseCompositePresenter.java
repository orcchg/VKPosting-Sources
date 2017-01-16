package com.orcchg.vikstra.app.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import hugo.weaving.DebugLog;

public abstract class BaseCompositePresenter<V extends MvpView> extends BasePresenter<V> {

    protected List<? extends MvpPresenter> presenterList;

    protected abstract List<? extends MvpPresenter> providePresenterList();

    @DebugLog @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (presenterList == null) presenterList = providePresenterList();
        for (MvpPresenter<V> presenter : presenterList) {
            presenter.attachView(getView());
            presenter.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        for (MvpPresenter<V> presenter : presenterList) presenter.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        for (MvpPresenter<V> presenter : presenterList) presenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        for (MvpPresenter<V> presenter : presenterList) presenter.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        for (MvpPresenter<V> presenter : presenterList) presenter.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        for (MvpPresenter<V> presenter : presenterList) presenter.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (MvpPresenter<V> presenter : presenterList) {
            presenter.onDestroy();
            presenter.detachView();
        }
        presenterList.clear();
        presenterList = null;
    }
}
