package com.orcchg.vikstra.app.ui.group.list.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.app.ui.common.view.PostThumbnail;
import com.orcchg.vikstra.app.ui.group.list.activity.injection.DaggerGroupListComponent;
import com.orcchg.vikstra.app.ui.group.list.activity.injection.GroupListComponent;
import com.orcchg.vikstra.app.ui.group.list.fragment.GroupListFragment;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupListActivity extends BaseActivity<GroupListContract.View, GroupListContract.Presenter>
        implements GroupListContract.View, ShadowHolder {
    private static final String FRAGMENT_TAG = "group_list_fragment_tag";
    private static final String EXTRA_KEYWORD_BUNDLE_ID = "extra_keyword_bundle_id";
    private static final String EXTRA_POST_ID = "extra_post_id";
    public static final int REQUEST_CODE = Constant.RequestCode.GROUP_LIST_SCREEN;

    private String DIALOG_TITLE, DIALOG_HINT, INFO_TITLE;

    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tv_info_title) TextView selectedGroupsCountView;
    @BindView(R.id.post_thumbnail) PostThumbnail postThumbnail;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;
    @OnClick(R.id.fab)
    void onPostFabClick() {
        presenter.onFabClick();
    }
    @OnClick(R.id.btn_add_keyword)
    void onAddKeywordClick() {
        presenter.onAddKeyword();
    }
    @OnClick(R.id.btn_change_post)
    void onChangePost() {
        navigationComponent.navigator().openPostListScreen(this);
    }

    private GroupListComponent groupComponent;
    private long keywordBundleId = Constant.BAD_ID;
    private long postId = Constant.BAD_ID;

    public static Intent getCallingIntent(@NonNull Context context, long keywordBunldeId, long postId) {
        Intent intent = new Intent(context, GroupListActivity.class);
        intent.putExtra(EXTRA_KEYWORD_BUNDLE_ID, keywordBunldeId);
        intent.putExtra(EXTRA_POST_ID, postId);
        return intent;
    }

    @NonNull @Override
    protected GroupListContract.Presenter createPresenter() {
        return groupComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        groupComponent = DaggerGroupListComponent.builder()
                .applicationComponent(getApplicationComponent())
                .build();
        groupComponent.inject(this);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData();  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        ButterKnife.bind(this);
        initResources();
        initView();
        initToolbar();
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    private void initData() {
        keywordBundleId = getIntent().getLongExtra(EXTRA_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
        postId = getIntent().getLongExtra(EXTRA_POST_ID, Constant.BAD_ID);
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        fab.hide();  // hide fab at fresh start before post fetched
        postThumbnail.setOnClickListener((view) -> navigationComponent.navigator().openPostViewScreen(this, postId));
        updateSelectedGroupsCounter(0, 0);

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(FRAGMENT_TAG) == null) {
            GroupListFragment fragment = GroupListFragment.newInstance(keywordBundleId, postId);
            fm.beginTransaction().replace(R.id.container, fragment, FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.group_list_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());
        toolbar.inflateMenu(R.menu.edit_dump);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.edit:
                    openEditTitleDialog(toolbar.getTitle().toString());
                    return true;
                case R.id.dump:
                    presenter.onDumpPressed();
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
    // --------------------------------------------------------------------------------------------
    @Override
    public void openEditTitleDialog(@Nullable String initTitle) {
        DialogProvider.showEditTextDialog(this, DIALOG_TITLE, DIALOG_HINT, initTitle,
                (dialog, which, text) -> {
                    toolbar.setTitle(text);
                    presenter.onTitleChanged(text);
                });
    }

    @Override
    public void setInputGroupsTitle(String title) {
        if (!TextUtils.isEmpty(title)) toolbar.setTitle(title);
    }

    /* Mediator */
    // ------------------------------------------
    @Override
    public void showEmptyPost() {
        fab.hide();
        postThumbnail.setPost(null);
    }

    @Override
    public void showPost(@Nullable PostSingleGridItemVO viewObject) {
        if (viewObject != null) fab.show();
        postThumbnail.setPost(viewObject);
    }

    @Override
    public void updateSelectedGroupsCounter(int count, int total) {
        String text = new StringBuilder(String.format(INFO_TITLE, count)).append('/').append(total).toString();
        selectedGroupsCountView.setText(text);
    }

    /* Resources */
    // --------------------------------------------------------------------------------------------
    private void initResources() {
        DIALOG_TITLE = getResources().getString(R.string.dialog_input_edit_title);
        DIALOG_HINT = getResources().getString(R.string.dialog_input_edit_title_hint);
        INFO_TITLE = getResources().getString(R.string.group_list_selected_groups_total_count);
    }
}
