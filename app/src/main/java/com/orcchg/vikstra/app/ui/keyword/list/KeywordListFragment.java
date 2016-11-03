package com.orcchg.vikstra.app.ui.keyword.list;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseListFragment;
import com.orcchg.vikstra.app.ui.keyword.list.injection.DaggerKeywordListComponent;
import com.orcchg.vikstra.app.ui.keyword.list.injection.KeywordListComponent;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class KeywordListFragment extends BaseListFragment<KeywordListContract.View, KeywordListContract.Presenter> implements KeywordListContract.View {

    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.empty_view) View emptyView;
    @BindView(R.id.loading_view) View loadingView;
    @BindView(R.id.error_view) View errorView;
    @OnClick(R.id.btn_retry)
    public void onRetryClick() {
        presenter.retry();
    }

    private KeywordListComponent keywordListComponent;

    private ShadowHolder shadowHolder;

    @NonNull @Override
    protected KeywordListContract.Presenter createPresenter() {
        return keywordListComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        keywordListComponent = DaggerKeywordListComponent.builder()
                .applicationComponent(getApplicationComponent())
                .build();
        keywordListComponent.inject(this);
    }

    public static KeywordListFragment newInstance() {
        KeywordListFragment fragment = new KeywordListFragment();
        // TODO:
        return fragment;
    }

    /* Lifecycle */
    // ------------------------------------------
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (ShadowHolder.class.isInstance(activity)) {
            shadowHolder = (ShadowHolder) activity;
        }
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_keywords_list, container, false);
        ButterKnife.bind(this, rootView);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_items);

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(() -> presenter.retry());

        return rootView;
    }

    /* Contract */
    // ------------------------------------------
    @Override
    public void showKeywords(List<KeywordListItemVO> keywords) {
        swipeRefreshLayout.setRefreshing(false);
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);

        if (keywords == null || keywords.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        if (shadowHolder != null) shadowHolder.showShadow(true);
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

    @Override
    protected void onScroll(int itemsLeftToEnd) {
        presenter.onScroll(itemsLeftToEnd);
    }
}
