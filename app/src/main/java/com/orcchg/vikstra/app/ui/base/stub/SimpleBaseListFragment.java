package com.orcchg.vikstra.app.ui.base.stub;

import android.support.annotation.NonNull;

import com.orcchg.vikstra.app.ui.base.BaseListFragment;
import com.orcchg.vikstra.app.ui.base.BasePresenter;

public class SimpleBaseListFragment extends BaseListFragment<StubMvpView, BasePresenter<StubMvpView>> {

    @NonNull @Override
    protected BasePresenter<StubMvpView> createPresenter() {
        return new BasePresenter<>();
    }

    @Override
    protected void injectDependencies() {
        // empty
    }

    @Override
    protected void onScroll(int itemsLeftToEnd) {
        // override in subclass
    }
}
