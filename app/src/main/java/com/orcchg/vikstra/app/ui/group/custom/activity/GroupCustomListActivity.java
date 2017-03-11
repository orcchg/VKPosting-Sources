package com.orcchg.vikstra.app.ui.group.custom.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.common.content.IListReach;
import com.orcchg.vikstra.app.ui.common.content.IScrollList;
import com.orcchg.vikstra.app.ui.common.view.PostThumbnail;
import com.orcchg.vikstra.app.ui.group.custom.activity.injection.DaggerGroupCustomListComponent;
import com.orcchg.vikstra.app.ui.group.custom.activity.injection.GroupCustomListComponent;
import com.orcchg.vikstra.app.ui.group.custom.activity.injection.GroupCustomListModule;
import com.orcchg.vikstra.app.ui.group.custom.fragment.GroupCustomListFragment;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.DomainConfig;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class GroupCustomListActivity extends BaseActivity<GroupCustomListContract.View, GroupCustomListContract.Presenter>
        implements GroupCustomListContract.View, IListReach, IScrollList, ShadowHolder {
    private static final String FRAGMENT_TAG = "group_custom_list_fragment_tag";
    private static final String BUNDLE_KEY_CHOSEN_SETTING_VARIANT = "bundle_key_chosen_setting_variant";
    private static final String BUNDLE_KEY_POST_ID = "bundle_key_post_id";
    private static final String EXTRA_POST_ID = "extra_post_id";
    public static final int REQUEST_CODE = Constant.RequestCode.GROUP_CUSTOM_LIST_SCREEN;

    @BindView(R.id.coordinator_root) ViewGroup coordinatorRoot;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;
    @BindView(R.id.tv_info_title) TextView selectedGroupsCountView;
    @BindView(R.id.btn_change_post) Button changePostButton;
    @BindView(R.id.post_thumbnail) PostThumbnail postThumbnail;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.fab_label) TextView fabLabel;
    @OnClick(R.id.fab)
    void onPostFabClick() {
        presenter.onFabClick();
    }
    @OnClick(R.id.btn_change_post)
    void onChangePostClick() {
        navigationComponent.navigator().openPostListScreen(this, postId);
    }

    private GroupCustomListComponent groupCustomListComponent;
    private long postId = Constant.BAD_ID;

    private int chosenSettingVariant = DomainConfig.INSTANCE.choosenVariantSleepInterval();

    public static Intent getCallingIntent(@NonNull Context context, long postId) {
        Intent intent = new Intent(context, GroupCustomListContract.class);
        intent.putExtra(EXTRA_POST_ID, postId);
        return intent;
    }

    @NonNull @Override
    protected GroupCustomListContract.Presenter createPresenter() {
        return groupCustomListComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        groupCustomListComponent = DaggerGroupCustomListComponent.builder()
                .applicationComponent(getApplicationComponent())
                .groupCustomListModule(new GroupCustomListModule())
                .build();
        groupCustomListComponent.inject(this);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData(savedInstanceState);  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        ButterKnife.bind(this);
        initResources();
        initView();
        initToolbar();
    }

    @Override
    public void onBackPressed() {
        presenter.onBackPressed();
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_KEY_CHOSEN_SETTING_VARIANT, chosenSettingVariant);
        outState.putLong(BUNDLE_KEY_POST_ID, postId);
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    private void initData(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Timber.d("Restore state");
            chosenSettingVariant = savedInstanceState.getInt(BUNDLE_KEY_CHOSEN_SETTING_VARIANT, DomainConfig.INSTANCE.choosenVariantSleepInterval());
            postId = savedInstanceState.getLong(BUNDLE_KEY_POST_ID, Constant.BAD_ID);
        } else {
            chosenSettingVariant = DomainConfig.INSTANCE.choosenVariantSleepInterval();
            postId = getIntent().getLongExtra(EXTRA_POST_ID, Constant.BAD_ID);
        }
        Timber.d("Post id: %s", postId);
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        showFab(false);  // hide fab at fresh start before post fetched
        postThumbnail.setOnClickListener((view) -> presenter.onPostThumbnailClick(postId));
        postThumbnail.setErrorRetryButtonClickListener((view) -> presenter.retryPost());
        updateSelectedGroupsCounter(0, 0);

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(FRAGMENT_TAG) == null) {
            GroupCustomListFragment fragment = GroupCustomListFragment.newInstance(postId);
            fm.beginTransaction().replace(R.id.fl_container, fragment, FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.group_custom_list_screen_title);
        toolbar.setNavigationOnClickListener((view) -> onBackPressed());  // finish with current result
    }

    @Override
    public void showShadow(boolean show) {
        dropshadowView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void enableAddKeywordButton(boolean isEnabled) {

    }

    @Override
    public void retryList() {

    }

    @Override
    public void onEmptyList() {

    }

    @Override
    public void onScrollList(int itemsLeftToEnd) {

    }

    @Override
    public RecyclerView getListView(int tag) {
        return null;
    }

    @Override
    public void hasReachedTop(boolean reached) {

    }

    @Override
    public void hasReachedBottom(boolean reached) {

    }

    @Override
    public void onGroupsNotSelected() {

    }

    @Override
    public void onPostNotSelected() {

    }

    @Override
    public void openEditTitleDialog(@Nullable String initTitle) {

    }

    @Override
    public void openPostCreateScreen(long postId) {

    }

    @Override
    public void openPostListScreen() {

    }

    @Override
    public String getInputGroupsTitle() {
        return null;
    }

    @Override
    public void setInputGroupsTitle(String title) {

    }

    @Override
    public void setCloseViewResult(int result) {

    }

    @Override
    public void setNewPostId(long postId) {

    }

    @Override
    public void showEmptyPost() {

    }

    @Override
    public void showErrorPost() {

    }

    @Override
    public void showPost(@Nullable PostSingleGridItemVO viewObject) {

    }

    @Override
    public void showPostingButton(boolean isVisible) {

    }

    @Override
    public void showPostingFailed() {

    }

    @Override
    public void showPostingStartedMessage(boolean isStarted) {

    }

    @Override
    public void updateSelectedGroupsCounter(int newCount, int total) {

    }

    /* Resources */
    // --------------------------------------------------------------------------------------------
    private void initResources() {
        Resources resources = getResources();
    }

    private void showFab(boolean isVisible) {
        if (isVisible) {
            fab.show();
            fabLabel.setVisibility(View.VISIBLE);
        } else {
            fab.hide();
            fabLabel.setVisibility(View.INVISIBLE);
        }
    }
}
