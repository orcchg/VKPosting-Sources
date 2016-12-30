package com.orcchg.vikstra.app.ui.base.stub;

import android.support.annotation.NonNull;

import com.orcchg.vikstra.app.ui.base.BaseDialogFragment;
import com.orcchg.vikstra.app.ui.base.BasePresenter;

public class SimpleBaseDialogFragment extends BaseDialogFragment<StubMvpView, BasePresenter<StubMvpView>> {

    @NonNull @Override
    protected BasePresenter<StubMvpView> createPresenter() {
        return new SimpleBasePresenter();
    }

    @Override
    protected void injectDependencies() {
        // empty
    }
}
