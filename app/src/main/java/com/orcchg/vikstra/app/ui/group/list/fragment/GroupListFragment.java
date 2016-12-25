package com.orcchg.vikstra.app.ui.group.list.fragment;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseListFragment;
import com.orcchg.vikstra.app.ui.common.injection.KeywordModule;
import com.orcchg.vikstra.app.ui.common.injection.PostModule;
import com.orcchg.vikstra.app.ui.group.list.activity.GroupListActivity;
import com.orcchg.vikstra.app.ui.group.list.fragment.injection.DaggerGroupListComponent;
import com.orcchg.vikstra.app.ui.group.list.fragment.injection.GroupListComponent;
import com.orcchg.vikstra.app.ui.report.ReportActivity;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;
import com.orcchg.vikstra.app.ui.util.UiUtility;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupListFragment extends BaseListFragment<GroupListContract.View, GroupListContract.Presenter>
        implements GroupListContract.View {
    private static final String BUNDLE_KEY_KEYWORDS_BUNDLE_ID = "bundle_key_keywords_bundle_id";
    private static final String BUNDLE_KEY_POST_ID = "bundle_key_post_id";
    public static final int RV_TAG = Constant.ListTag.GROUP_LIST_SCREEN;

    private String NOTIFICATION_POSTING_COMPLETE, NOTIFICATION_PHOTO_UPLOAD_COMPLETE, SNACKBAR_KEYWORDS_LIMIT;

    private ItemTouchHelper itemTouchHelper;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.empty_view) View emptyView;
    @BindView(R.id.loading_view) View loadingView;
    @BindView(R.id.error_view) View errorView;
    @OnClick(R.id.btn_retry)
    void onRetryClick() {
        presenter.retry();
    }

//    private FabHolder fabHolder;
//    private GroupsCounterHolder groupsCounterHolder;
//    private PostThumbHolder postThumbHolder;
    private ShadowHolder shadowHolder;

    private GroupListComponent groupComponent;
    private long keywordBundleId = Constant.BAD_ID;
    private long postId = Constant.BAD_ID;

    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notificationBuilderPosting;
    NotificationCompat.Builder notificationBuilderPhotoUpload;

    @NonNull @Override
    protected GroupListContract.Presenter createPresenter() {
        return groupComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        groupComponent = DaggerGroupListComponent.builder()
                .applicationComponent(getApplicationComponent())
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

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (ShadowHolder.class.isInstance(context)) shadowHolder = (ShadowHolder) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Resources resources = getResources();
        initResources();
        initItemTouchHelper();
        Bundle args = getArguments();
        keywordBundleId = args.getLong(BUNDLE_KEY_KEYWORDS_BUNDLE_ID, Constant.BAD_ID);
        postId = args.getLong(BUNDLE_KEY_POST_ID, Constant.BAD_ID);
        super.onCreate(savedInstanceState);

        Intent intent = ReportActivity.getCallingIntent(getActivity(), postId);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), GroupListActivity.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationManager = NotificationManagerCompat.from(getActivity());
        notificationBuilderPosting = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.ic_cloud_upload_white_18dp)
                .setContentTitle(resources.getString(R.string.group_list_notification_posting_title))
                .setContentText(resources.getString(R.string.group_list_notification_posting_description_progress))
                .setContentIntent(pendingIntent);
        notificationBuilderPhotoUpload = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.ic_collections_white_18dp)
                .setContentTitle(resources.getString(R.string.group_list_notification_photo_upload_title))
                .setContentText(resources.getString(R.string.group_list_notification_photo_upload_description_progress));
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_group_list, container, false);
        ButterKnife.bind(this, rootView);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_items);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        swipeRefreshLayout.setColorSchemeColors(UiUtility.getAttributeColor(getActivity(), R.attr.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(() -> presenter.retry());

        return rootView;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onScroll(int itemsLeftToEnd) {
        // TODO: impl
    }

    @Override
    public void onAddKeywordError() {
        UiUtility.showSnackbar(getActivity(), R.string.group_list_error_add_keyword);
    }

    @Override
    public void onKeywordsLimitReached(int limit) {
        UiUtility.showSnackbar(getActivity(), String.format(SNACKBAR_KEYWORDS_LIMIT, limit));
    }

    @Override
    public void openGroupDetailScreen(long groupId) {
        navigationComponent.navigator().openGroupDetailScreen(getActivity(), groupId);
    }

    @Override
    public void openReportScreen(long postId) {
        navigationComponent.navigator().openReportScreen(getActivity(), postId);
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
    public void showGroups(boolean isEmpty) {
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

    // ------------------------------------------
    @Override
    public void onPostingProgress(int progress, int total) {
        notificationBuilderPosting.setProgress(progress, total, false);
        notificationManager.notify(Constant.NotificationID.GROUP_LIST_SCREEN_POSTING, notificationBuilderPosting.build());
    }

    @Override
    public void onPostingProgressInfinite() {
        notificationBuilderPosting.setProgress(0, 0, true);
        notificationManager.notify(Constant.NotificationID.GROUP_LIST_SCREEN_POSTING, notificationBuilderPosting.build());
    }

    @Override
    public void onPostingComplete() {
        notificationBuilderPosting.setContentText(NOTIFICATION_POSTING_COMPLETE).setProgress(0, 0, false);
        notificationManager.notify(Constant.NotificationID.GROUP_LIST_SCREEN_POSTING, notificationBuilderPosting.build());
    }

    // ------------------------------------------
    @Override
    public void onPhotoUploadProgress(int progress, int total) {
        notificationBuilderPhotoUpload.setProgress(progress, total, false);
        notificationManager.notify(Constant.NotificationID.GROUP_LIST_SCREEN_PHOTO_UPLOAD, notificationBuilderPhotoUpload.build());
    }

    @Override
    public void onPhotoUploadProgressInfinite() {
        notificationBuilderPhotoUpload.setProgress(0, 0, true);
        notificationManager.notify(Constant.NotificationID.GROUP_LIST_SCREEN_PHOTO_UPLOAD, notificationBuilderPhotoUpload.build());
    }

    @Override
    public void onPhotoUploadComplete() {
        notificationBuilderPhotoUpload.setContentText(NOTIFICATION_PHOTO_UPLOAD_COMPLETE).setProgress(0, 0, false);
        notificationManager.notify(Constant.NotificationID.GROUP_LIST_SCREEN_PHOTO_UPLOAD, notificationBuilderPhotoUpload.build());
    }

    /* Resources */
    // --------------------------------------------------------------------------------------------
    private void initResources() {
        Resources resources = getResources();
        NOTIFICATION_POSTING_COMPLETE = resources.getString(R.string.group_list_notification_posting_description_complete);
        NOTIFICATION_PHOTO_UPLOAD_COMPLETE = resources.getString(R.string.group_list_notification_photo_upload_description_complete);
        SNACKBAR_KEYWORDS_LIMIT = resources.getString(R.string.group_list_snackbar_keywords_limit_message);
    }

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
