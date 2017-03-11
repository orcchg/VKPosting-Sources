package com.orcchg.vikstra.app.ui.group.custom.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.common.screen.CollectionFragment;
import com.orcchg.vikstra.app.ui.group.custom.fragment.injection.DaggerGroupCustomListComponent;
import com.orcchg.vikstra.app.ui.group.custom.fragment.injection.GroupCustomListComponent;
import com.orcchg.vikstra.app.ui.group.custom.fragment.injection.GroupCustomListModule;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.Collection;

import butterknife.OnClick;

public class GroupCustomListFragment extends CollectionFragment<GroupCustomListContract.View, GroupCustomListContract.Presenter>
        implements GroupCustomListContract.View {
    private static final String BUNDLE_KEY_POST_ID = "bundle_key_post_id";
    public static final int RV_TAG = Constant.ListTag.GROUP_CUSTOM_LIST_SCREEN;

    @OnClick(R.id.btn_retry)
    void onRetryClick() {
        presenter.retry();  // override
    }

    private GroupCustomListComponent groupCustomListComponent;
    private long postId = Constant.BAD_ID;

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

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    public static GroupCustomListFragment newInstance(long postId) {
        Bundle args = new Bundle();
        args.putLong(BUNDLE_KEY_POST_ID, postId);
        GroupCustomListFragment fragment = new GroupCustomListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected boolean isGrid() {
        return false;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        postId = args.getLong(BUNDLE_KEY_POST_ID, Constant.BAD_ID);
        super.onCreate(savedInstanceState);
    }








    @Override
    public void onAccessTokenExhausted() {

    }

    @Override
    public void enableSwipeToRefresh(boolean isEnabled) {

    }

    @Override
    public void onSearchingGroupsCancel() {

    }

    @Override
    public void openInteractiveReportScreen(long keywordBundleId, long postId) {

    }

    @Override
    public void openGroupDetailScreen(long groupId) {

    }

    @Override
    public void showGroups(boolean isEmpty) {

    }

    @Override
    public void startWallPostingService(long keywordBundleId, Collection<Group> selectedGroups, Post post) {

    }
}
