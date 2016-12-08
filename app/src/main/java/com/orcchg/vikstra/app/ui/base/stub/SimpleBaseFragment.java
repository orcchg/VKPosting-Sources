package com.orcchg.vikstra.app.ui.base.stub;

import android.support.annotation.NonNull;

import com.orcchg.vikstra.app.ui.base.BaseFragment;
import com.orcchg.vikstra.app.ui.base.BasePresenter;

public abstract class SimpleBaseFragment extends BaseFragment<StubMvpView, BasePresenter<StubMvpView>> {

    @NonNull @Override
    protected BasePresenter<StubMvpView> createPresenter() {
        return new SimpleBasePresenter();
    }

    @Override
    protected void injectDependencies() {
        // empty
    }
}
