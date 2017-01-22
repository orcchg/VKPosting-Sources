package com.orcchg.vikstra.app.ui.group.list.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.AppConfig;
import com.orcchg.vikstra.app.ui.common.injection.KeywordModule;
import com.orcchg.vikstra.app.ui.common.injection.PostModule;
import com.orcchg.vikstra.app.ui.common.notification.PhotoUploadNotification;
import com.orcchg.vikstra.app.ui.common.notification.PostingNotification;
import com.orcchg.vikstra.app.ui.common.screen.CollectionFragment;
import com.orcchg.vikstra.app.ui.group.list.fragment.injection.DaggerGroupListComponent;
import com.orcchg.vikstra.app.ui.group.list.fragment.injection.GroupListComponent;
import com.orcchg.vikstra.app.ui.group.list.fragment.injection.GroupListModule;
import com.orcchg.vikstra.app.ui.status.StatusDialogFragment;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.OnClick;

public class GroupListFragment extends CollectionFragment<GroupListContract.View, GroupListContract.Presenter>
        implements GroupListContract.View {
    private static final String BUNDLE_KEY_KEYWORDS_BUNDLE_ID = "bundle_key_keywords_bundle_id";
    private static final String BUNDLE_KEY_POST_ID = "bundle_key_post_id";
    public static final int RV_TAG = Constant.ListTag.GROUP_LIST_SCREEN;

    private ItemTouchHelper itemTouchHelper;
    @OnClick(R.id.btn_retry)
    void onRetryClick() {
        presenter.retry();  // override
    }

    private PostingNotification postingNotification;
    private PhotoUploadNotification photoUploadNotification;

    private GroupListComponent groupComponent;
    private long keywordBundleId = Constant.BAD_ID;
    private long postId = Constant.BAD_ID;

    @NonNull @Override
    protected GroupListContract.Presenter createPresenter() {
        return groupComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        groupComponent = DaggerGroupListComponent.builder()
                .applicationComponent(getApplicationComponent())
                .groupListModule(new GroupListModule(Constant.BAD_ID))  // proper id will be set later
                .keywordModule(new KeywordModule(keywordBundleId))
                .postModule(new PostModule(postId))
                .build();
        groupComponent.inject(this);
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    public static GroupListFragment newInstance(long keywordsBundleId, long postId) {
        Bundle args = new Bundle();
        args.putLong(BUNDLE_KEY_KEYWORDS_BUNDLE_ID, keywordsBundleId);
        args.putLong(BUNDLE_KEY_POST_ID, postId);
        GroupListFragment fragment = new GroupListFragment();
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
        initItemTouchHelper();
        Bundle args = getArguments();
        keywordBundleId = args.getLong(BUNDLE_KEY_KEYWORDS_BUNDLE_ID, Constant.BAD_ID);
        postId = args.getLong(BUNDLE_KEY_POST_ID, Constant.BAD_ID);
        super.onCreate(savedInstanceState);
        initNotifications();
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(() -> presenter.refresh());  // override
        itemTouchHelper.attachToRecyclerView(recyclerView);
        return rootView;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void enableSwipeToRefresh(boolean isEnabled) {
        swipeRefreshLayout.setEnabled(isEnabled);
    }

    @Override
    public void onReportReady(long groupReportBundleId, long postId) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        StatusDialogFragment dialog = (StatusDialogFragment) fm.findFragmentByTag(StatusDialogFragment.DIALOG_TAG);
        if (dialog != null) dialog.onReportReady(groupReportBundleId, postId);
    }

    @Override
    public void openInteractiveReportScreen(long postId) {
        navigationComponent.navigator().openReportScreen(getActivity(), postId);
    }

    @Override
    public void openGroupDetailScreen(long groupId) {
        navigationComponent.navigator().openGroupDetailScreen(getActivity(), groupId);
    }

    @Override
    public void openStatusScreen() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        navigationComponent.navigator().openStatusDialog(fm, StatusDialogFragment.DIALOG_TAG);
    }

    // ------------------------------------------
    @Override
    public void showGroups(boolean isEmpty) {
        showContent(RV_TAG, isEmpty);
    }

    @Override
    public void showProgressDialog(boolean isVisible) {
        // TODO: is need?
    }

    @Override
    public void updateGroupReportBundleId(long groupReportBundleId) {
        postingNotification.updateGroupReportBundleId(getActivity(), groupReportBundleId);
    }

    /* Notification delegate */
    // --------------------------------------------------------------------------------------------
    private void initNotifications() {
        postingNotification = new PostingNotification(getActivity(), Constant.BAD_ID, postId);
        photoUploadNotification = new PhotoUploadNotification(getActivity());
    }

    // ------------------------------------------
    @Override
    public void onPostingProgress(int progress, int total) {
        postingNotification.onPostingProgress(progress, total);

        if (!AppConfig.INSTANCE.useInteractiveReportScreen()) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            StatusDialogFragment dialog = (StatusDialogFragment) fm.findFragmentByTag(StatusDialogFragment.DIALOG_TAG);
            if (dialog != null) dialog.updatePostingProgress(progress, total);
        }
    }

    @Override
    public void onPostingProgressInfinite() {
        postingNotification.onPostingProgressInfinite();
    }

    @Override
    public void onPostingComplete() {
        postingNotification.onPostingComplete();

        if (!AppConfig.INSTANCE.useInteractiveReportScreen()) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            StatusDialogFragment dialog = (StatusDialogFragment) fm.findFragmentByTag(StatusDialogFragment.DIALOG_TAG);
            if (dialog != null) dialog.onPostingComplete();
        }
    }

    // ------------------------------------------
    @Override
    public void onPhotoUploadProgress(int progress, int total) {
        photoUploadNotification.onPhotoUploadProgress(progress, total);
    }

    @Override
    public void onPhotoUploadProgressInfinite() {
        photoUploadNotification.onPhotoUploadProgressInfinite();
    }

    @Override
    public void onPhotoUploadComplete() {
        photoUploadNotification.onPhotoUploadComplete();
    }

    /* Resources */
    // --------------------------------------------------------------------------------------------
    private void initItemTouchHelper() {
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                presenter.removeListItem(viewHolder.getAdapterPosition());
            }
        });
    }
}
