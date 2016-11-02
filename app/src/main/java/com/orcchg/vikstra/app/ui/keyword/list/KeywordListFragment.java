package com.orcchg.vikstra.app.ui.keyword.list;

import android.support.annotation.NonNull;

import com.orcchg.vikstra.app.ui.base.BaseFragment;

public class KeywordListFragment extends BaseFragment<KeywordListContract.View, KeywordListContract.Presenter> implements KeywordListContract.View {

    @NonNull @Override
    protected KeywordListContract.Presenter createPresenter() {
        return null;
    }

    @Override
    protected void injectDependencies() {

    }

    public static KeywordListFragment newInstance() {
        KeywordListFragment fragment = new KeywordListFragment();
        // TODO:
        return fragment;
    }

    /* Lifecycle */
    // ------------------------------------------
}
