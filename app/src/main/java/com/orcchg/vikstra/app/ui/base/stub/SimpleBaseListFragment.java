package com.orcchg.vikstra.app.ui.base.stub;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;

import com.orcchg.vikstra.app.ui.base.BaseListFragment;
import com.orcchg.vikstra.app.ui.base.BasePresenter;

public class SimpleBaseListFragment extends BaseListFragment<StubMvpView, BasePresenter<StubMvpView>> {

    @NonNull @Override
    protected BasePresenter<StubMvpView> createPresenter() {
        return new SimpleBasePresenter();
    }

    @Override
    protected void injectDependencies() {
        // empty
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    protected void onScroll(int itemsLeftToEnd) {
        // override in subclass
    }
}
