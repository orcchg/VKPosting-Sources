package com.orcchg.vikstra.app.ui.common.screen;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseListFragment;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.app.ui.common.content.IListReach;
import com.orcchg.vikstra.app.ui.common.content.IScrollGrid;
import com.orcchg.vikstra.app.ui.common.content.IScrollList;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;
import com.orcchg.vikstra.app.ui.util.UiUtility;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public abstract class CollectionFragment<V extends MvpView, P extends MvpPresenter<V>> extends BaseListFragment<V, P>
        implements LceView {

    protected @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    protected @BindView(R.id.empty_view) View emptyView;
    protected @BindView(R.id.error_view) View errorView;
    protected @BindView(R.id.loading_view) View loadingView;
    protected @BindView(R.id.tv_error) TextView errorTextView;
    protected @BindView(R.id.tv_empty_data) TextView emptyDataTextView;
    protected @BindView(R.id.btn_retry) Button errorRetryButton;
    protected @BindView(R.id.btn_empty_data) Button emptyDataButton;
    protected @OnClick(R.id.btn_empty_data)
    void onEmptyDataClick() {
        if (isGrid()) {
            if (iScrollGrid != null) iScrollGrid.onEmptyGrid();
        } else {
            if (iScrollList != null) iScrollList.onEmptyList();
        }
    }
    @OnClick(R.id.btn_retry)
    void onRetryClick() {
        if (isGrid()) {
            if (iScrollGrid != null) iScrollGrid.retryGrid();
        } else {
            if (iScrollList != null) iScrollList.retryList();
        }
    }

    protected IListReach iListReach;
    protected IScrollGrid iScrollGrid;
    protected IScrollList iScrollList;
    protected ShadowHolder shadowHolder;

    protected abstract boolean isGrid();
    protected boolean autoFit() {
        return false;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (IListReach.class.isInstance(context))   iListReach   = (IListReach) context;
        if (IScrollGrid.class.isInstance(context))  iScrollGrid  = (IScrollGrid) context;
        if (IScrollList.class.isInstance(context))  iScrollList  = (IScrollList) context;
        if (ShadowHolder.class.isInstance(context)) shadowHolder = (ShadowHolder) context;
    }

    @DebugLog @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        @LayoutRes int layout = autoFit() ? R.layout.collection_layout_autofit : R.layout.collection_layout;
        View rootView = inflater.inflate(layout, container, false);
        ButterKnife.bind(this, rootView);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_items);

        swipeRefreshLayout.setColorSchemeColors(UiUtility.getAttributeColor(getActivity(), R.attr.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (isGrid()) {
                if (iScrollGrid != null) iScrollGrid.retryGrid();
            } else {
                if (iScrollList != null) iScrollList.retryList();
            }
        });

        return rootView;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onScroll(int itemsLeftToEnd) {
        if (isGrid()) {
            if (iScrollGrid != null) iScrollGrid.onScrollGrid(itemsLeftToEnd);
        } else {
            if (iScrollList != null) iScrollList.onScrollList(itemsLeftToEnd);
        }
        if (iListReach != null) iListReach.hasReachedBottom(isListReachedBottom());
    }

    @Override
    protected void onScrollTop() {
        if (iListReach != null) iListReach.hasReachedTop(isListReachedTop());
    }

    @Override
    public boolean isContentViewVisible(int tag) {
        return UiUtility.isVisible(recyclerView);
    }

    @Override
    public void showContent(int tag, boolean isEmpty) {
        if (!isEmpty && UiUtility.isVisible(recyclerView)) {
            Timber.v("List items are already visible");
            return;
        }

        swipeRefreshLayout.setRefreshing(false);
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);

        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        if (shadowHolder != null) shadowHolder.showShadow(true);
    }

    @Override
    public void showEmptyList(int tag) {
        showContent(tag, true);
    }

    @Override
    public void showError(int tag) {
        swipeRefreshLayout.setRefreshing(false);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);

        if (shadowHolder != null) shadowHolder.showShadow(true);
    }

    @Override
    public void showLoading(int tag) {
        swipeRefreshLayout.setRefreshing(false);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);

        if (shadowHolder != null) shadowHolder.showShadow(false);  // don't overlap with progress bar
    }
}
