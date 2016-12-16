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
import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.common.content.IScrollGrid;
import com.orcchg.vikstra.app.ui.common.content.IScrollList;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListAdapter;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListFragment;
import com.orcchg.vikstra.app.ui.main.injection.DaggerMainComponent;
import com.orcchg.vikstra.app.ui.main.injection.MainComponent;
import com.orcchg.vikstra.app.ui.main.injection.MainModule;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridFragment;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity<MainContract.View, MainContract.Presenter>
        implements MainContract.View, IScrollGrid, IScrollList {
    private static final String POST_GRID_FRAGMENT_TAG = "post_grid_fragment_tag";
    private static final String KEYW_LIST_FRAGMENT_TAG = "keyw_list_fragment_tag";

    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.tv_groups_selection_counter) TextView selectedGroupsTextView;
    @OnClick(R.id.fab)
    void onFabClick() {
        presenter.onFabClick();
    }
    @OnClick(R.id.ibtn_see_all_keywords)
    void onAddNewGroupsClick() {
        navigationComponent.navigator().openKeywordListScreen(this);
    }
    @OnClick(R.id.btn_new_keywords)
    public void onNewKeywordsClick() {
        navigationComponent.navigator().openKeywordCreateScreen(this);
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
                .mainModule(new MainModule(KeywordListAdapter.SELECT_MODE_SINGLE))  // items are selectable
                .build();
        mainComponent.inject(this);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
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
    // --------------------------------------------------------------------------------------------
    private void initView() {
        fab.hide();  // hide fab at fresh start because nothing has been selected yet

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(POST_GRID_FRAGMENT_TAG) == null) {
            PostSingleGridFragment fragment = PostSingleGridFragment.newInstance();
            fm.beginTransaction().replace(R.id.fl_top, fragment, POST_GRID_FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }
        if (fm.findFragmentByTag(KEYW_LIST_FRAGMENT_TAG) == null) {
            KeywordListFragment fragment = KeywordListFragment.newInstance();
            fm.beginTransaction().replace(R.id.fl_bottom, fragment, KEYW_LIST_FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public RecyclerView getListView(int tag) {
        MvpListView fragment = null;
        switch (tag) {
            case KeywordListFragment.RV_TAG:     fragment = getKeywordListFragment();  break;
            case PostSingleGridFragment.RV_TAG:  fragment = getPostGridFragment();     break;
        }
        if (fragment != null) return fragment.getListView(tag);
        return null;
    }

    @Override
    public void openKeywordCreateScreen(long keywordBundleId) {
        navigationComponent.navigator().openKeywordCreateScreen(this, keywordBundleId);
    }

    @Override
    public void openGroupListScreen(long keywordBundleId) {
        navigationComponent.navigator().openGroupListScreen(this, keywordBundleId);
    }

    @Override
    public void openPostCreateScreen() {
        navigationComponent.navigator().openPostCreateScreen(this);
    }

    @Override
    public void openPostViewScreen(long postId) {
        navigationComponent.navigator().openPostViewScreen(this, postId);
    }

    @Override
    public void showFab(boolean isVisible) {
        if (isVisible) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    @Override
    public void showPosts(boolean isEmpty) {
        PostSingleGridFragment fragment = getPostGridFragment();
        if (fragment != null) fragment.showPosts(isEmpty);
    }

    @Override
    public void showKeywords(boolean isEmpty) {
        KeywordListFragment fragment = getKeywordListFragment();
        if (fragment != null) fragment.showKeywords(isEmpty);
    }

    @Override
    public void showEmptyList() {
        KeywordListFragment fragment = getKeywordListFragment();
        if (fragment != null) fragment.showEmptyList();
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

    // ------------------------------------------
    @Override
    public void retryList() {
        presenter.retryKeywords();
    }

    @Override
    public void onEmptyList() {
        navigationComponent.navigator().openKeywordCreateScreen(this);
    }

    @Override
    public void onScrollList(int itemsLeftToEnd) {
        presenter.onScrollKeywordsList(itemsLeftToEnd);
    }

    // ------------------------------------------
    @Override
    public void retryGrid() {
        presenter.retryPosts();
    }

    @Override
    public void onEmptyGrid() {
        // TODO: empty grid
    }

    @Override
    public void onScrollGrid(int itemsLeftToEnd) {
        // TODO: scroll grid
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Nullable
    private KeywordListFragment getKeywordListFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (KeywordListFragment) fm.findFragmentByTag(KEYW_LIST_FRAGMENT_TAG);
    }

    @Nullable
    private PostSingleGridFragment getPostGridFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (PostSingleGridFragment) fm.findFragmentByTag(POST_GRID_FRAGMENT_TAG);
    }
}
