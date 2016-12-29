package com.orcchg.vikstra.app.ui.common.screen;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.base.stub.PassiveView;
import com.orcchg.vikstra.app.ui.base.stub.SimpleBaseListFragment;
import com.orcchg.vikstra.app.ui.base.stub.SimpleBasePresenter;
import com.orcchg.vikstra.app.ui.base.stub.StubMvpView;
import com.orcchg.vikstra.app.ui.common.content.IScrollGrid;
import com.orcchg.vikstra.app.ui.common.content.IScrollList;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public abstract class SimpleCollectionFragment extends CollectionFragment<StubMvpView, BasePresenter<StubMvpView>>
        implements PassiveView {

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
