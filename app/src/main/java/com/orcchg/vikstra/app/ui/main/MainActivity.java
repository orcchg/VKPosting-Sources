package com.orcchg.vikstra.app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.common.content.IScrollList;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListFragment;
import com.orcchg.vikstra.app.ui.main.injection.DaggerMainComponent;
import com.orcchg.vikstra.app.ui.main.injection.MainComponent;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity<MainContract.View, MainContract.Presenter>
        implements MainContract.View, IScrollList {
    private static final String LIST_FRAGMENT_TAG = "list_fragment_tag";

    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.tv_groups_selection_counter) TextView selectedGroupsTextView;
    @OnClick(R.id.btn_add_new_groups)
    public void onAddNewGroupsClick() {
        navigationComponent.navigator().openKeywordsListScreen(this);
    }
    @OnClick(R.id.btn_groups_selection_drop)
    public void onDropSelectedGroupsClick() {
        // TODO: drop selection
    }

    private MainComponent mainComponent;

    @NonNull @Override
    protected MainContract.Presenter createPresenter() {
        return mainComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        mainComponent = DaggerMainComponent.builder()
                .applicationComponent(getApplicationComponent())
                .build();
        mainComponent.inject(this);
    }

    /* Lifecycle */
    // ------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VKSdk.login(this);  // TODO: scopes
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // TODO: User passed Authorization
            }

            @Override
            public void onError(VKError error) {
                // TODO: User didn't pass Authorization
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /* View */
    // ------------------------------------------
    private void initView() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(LIST_FRAGMENT_TAG) == null) {
            KeywordListFragment fragment = KeywordListFragment.newInstance();
            fm.beginTransaction().replace(R.id.fl_bottom, fragment, LIST_FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }

        // TODO: add fragments and change fab click listener
        fab.setOnClickListener((view) -> navigationComponent.navigator().openGroupsListScreen(this));
    }

    /* Contract */
    // ------------------------------------------
    @Override
    public RecyclerView getListView() {
        KeywordListFragment fragment = getKeywordListFragment();
        if (fragment != null) return fragment.getListView();
        return null;
    }

    @Override
    public void showKeywords(List<KeywordListItemVO> keywords) {
        KeywordListFragment fragment = getKeywordListFragment();
        if (fragment != null) fragment.showKeywords(keywords);
    }

    @Override
    public void showError() {
        KeywordListFragment fragment = getKeywordListFragment();
        if (fragment != null) fragment.showError();
    }

    @Override
    public void showLoading() {
        KeywordListFragment fragment = getKeywordListFragment();
        if (fragment != null) fragment.showLoading();
    }

    @Override
    public void retry() {
        presenter.retry();
    }

    @Override
    public void onScroll(int itemsLeftToEnd) {
        presenter.onScroll(itemsLeftToEnd);
    }

    /* Internal */
    // ------------------------------------------
    @Nullable
    private KeywordListFragment getKeywordListFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (KeywordListFragment) fm.findFragmentByTag(LIST_FRAGMENT_TAG);
    }
}
