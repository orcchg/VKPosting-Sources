package com.orcchg.vikstra.app.ui.keyword.list;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.stub.SimpleBaseListFragment;
import com.orcchg.vikstra.app.ui.common.content.IScrollList;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;
import com.orcchg.vikstra.app.ui.util.UiUtility;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class KeywordListFragment extends SimpleBaseListFragment implements KeywordListContract.SubView {
    public static final int RV_TAG = Constant.ListTag.KEYWORD_LIST_SCREEN;

    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.empty_view) View emptyView;
    @BindView(R.id.loading_view) View loadingView;
    @BindView(R.id.error_view) View errorView;
    @OnClick(R.id.btn_empty_data)
    void onEmptyDataClick() {
        iScrollList.onEmptyList();
    }
    @OnClick(R.id.btn_retry)
    void onRetryClick() {
        iScrollList.retryList();
    }

    private IScrollList iScrollList;
    private ShadowHolder shadowHolder;

    public static KeywordListFragment newInstance() {
        return new KeywordListFragment();
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (IScrollList.class.isInstance(context)) {
            iScrollList = (IScrollList) context;
        } else {
            String message = "Hosting Activity must implement IScrollList interface";
            Timber.e(message);
            throw new RuntimeException(message);
        }
        if (ShadowHolder.class.isInstance(context)) {
            shadowHolder = (ShadowHolder) context;
        }
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_keywords_list, container, false);
        ButterKnife.bind(this, rootView);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_items);

        swipeRefreshLayout.setColorSchemeColors(UiUtility.getAttributeColor(getActivity(), R.attr.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(() -> iScrollList.retryList());

        return rootView;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void showKeywords(boolean isEmpty) {
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
    public void showEmptyList() {
        showKeywords(true);
    }

    @Override
    public void showError() {
        swipeRefreshLayout.setRefreshing(false);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);

        if (shadowHolder != null) shadowHolder.showShadow(true);
    }

    @Override
    public void showLoading() {
        swipeRefreshLayout.setRefreshing(false);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);

        if (shadowHolder != null) shadowHolder.showShadow(false);  // don't overlap with progress bar
    }
}
