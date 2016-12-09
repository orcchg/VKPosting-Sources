package com.orcchg.vikstra.app.ui.keyword.list;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.common.content.IScrollList;
import com.orcchg.vikstra.app.ui.keyword.create.KeywordCreateActivity;
import com.orcchg.vikstra.app.ui.keyword.list.injection.DaggerKeywordListComponent;
import com.orcchg.vikstra.app.ui.keyword.list.injection.KeywordListComponent;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class KeywordListActivity extends BaseActivity<KeywordListContract.View, KeywordListContract.Presenter>
        implements KeywordListContract.View, IScrollList, ShadowHolder {
    public static final int REQUEST_CODE = Constant.RequestCode.KEYWORD_LIST_SCREEN;
    private static final String FRAGMENT_TAG = "keyword_list_fragment_tag";

    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;

    private KeywordListComponent keywordListComponent;

    public static Intent getCallingIntent(@NonNull Context context) {
        return new Intent(context, KeywordListActivity.class);
    }

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

    /* Lifecycle */
    // ------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keywords_list);
        ButterKnife.bind(this);
        initView();
        initToolbar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case KeywordCreateActivity.REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    presenter.retry();  // refresh keywords list
                    setResult(Activity.RESULT_OK);  // keywords list has changed at this screen
                }
                break;
        }
    }

    /* View */
    // ------------------------------------------
    private void initView() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(FRAGMENT_TAG) == null) {
            KeywordListFragment fragment = KeywordListFragment.newInstance();
            fm.beginTransaction().replace(R.id.container, fragment, FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }

        fab.setImageResource(R.drawable.ic_add_white_24dp);
        fab.setOnClickListener((view) -> navigationComponent.navigator().openKeywordCreateScreen(this));
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.keyword_list_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());  // close screen with current result
        toolbar.inflateMenu(R.menu.search);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.search:
                    // TODO: search
                    return true;
            }
            return false;
        });
    }

    @Override
    public void showShadow(boolean show) {
        dropshadowView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    /* Contract */
    // ------------------------------------------
    @Override
    public RecyclerView getListView() {
        KeywordListFragment fragment = getFragment();
        if (fragment != null) return fragment.getListView();
        return null;
    }

    @Override
    public void openKeywordCreateScreen(long keywordBundleId) {
        navigationComponent.navigator().openKeywordCreateScreen(this, keywordBundleId);
    }

    @Override
    public void showKeywords(List<KeywordListItemVO> keywords) {
        KeywordListFragment fragment = getFragment();
        if (fragment != null) fragment.showKeywords(keywords);
    }

    @Override
    public void showEmptyList() {
        KeywordListFragment fragment = getFragment();
        if (fragment != null) fragment.showEmptyList();
    }

    @Override
    public void showError() {
        KeywordListFragment fragment = getFragment();
        if (fragment != null) fragment.showError();
    }

    @Override
    public void showLoading() {
        KeywordListFragment fragment = getFragment();
        if (fragment != null) fragment.showLoading();
    }

    @Override
    public void retry() {
        presenter.retry();
    }

    @Override
    public void onEmpty() {
        navigationComponent.navigator().openKeywordCreateScreen(this);
    }

    @Override
    public void onScroll(int itemsLeftToEnd) {
        presenter.onScroll(itemsLeftToEnd);
    }

    /* Internal */
    // ------------------------------------------
    @Nullable
    private KeywordListFragment getFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (KeywordListFragment) fm.findFragmentByTag(FRAGMENT_TAG);
    }
}
