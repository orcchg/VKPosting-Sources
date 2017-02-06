package com.orcchg.vikstra.app.ui.report;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orcchg.vikstra.app.AppConfig;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.viewobject.ReportListItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.GroupReportEssenceToVoMapper;
import com.orcchg.vikstra.app.ui.viewobject.mapper.GroupReportToVoMapper;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.data.source.memory.ContentUtility;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.exception.vkontakte.Api220VkUseCaseException;
import com.orcchg.vikstra.domain.exception.vkontakte.Api5VkUseCaseException;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.Ordered;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.report.DumpGroupReports;
import com.orcchg.vikstra.domain.interactor.report.GetGroupReportBundleById;
import com.orcchg.vikstra.domain.interactor.report.PutGroupReportBundle;
import com.orcchg.vikstra.domain.interactor.vkontakte.MakeWallPost;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.Heavy;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.model.essense.mapper.GroupReportEssenceMapper;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.endpoint.EndpointUtility;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class ReportPresenter extends BaseListPresenter<ReportContract.View> implements ReportContract.Presenter {
    private static final int PrID = Constant.PresenterId.REPORT_PRESENTER;

    private final GetGroupReportBundleById getGroupReportBundleByIdUseCase;
    private final GetPostById getPostByIdUseCase;
    private final DumpGroupReports dumpGroupReportsUseCase;
    private final PutGroupReportBundle putGroupReportBundleUseCase;
    private final VkontakteEndpoint vkontakteEndpoint;

    private final GroupReportToVoMapper groupReportToVoMapper;
    private final GroupReportEssenceMapper groupReportEssenceMapper;
    private final GroupReportEssenceToVoMapper groupReportEssenceToVoMapper;
    private final PostToSingleGridVoMapper postToSingleGridVoMapper;

    // used only in interactive mode {@link InteractiveMode}
    private final MultiUseCase.ProgressCallback<GroupReportEssence> postingProgressCallback;
    private final MultiUseCase.CancelCallback postingCancelledCallback;
    private final MultiUseCase.FinishCallback postingFinishedCallback;

    private @InteractiveMode @Heavy List<GroupReport> storedReports = new ArrayList<>();

    private Memento memento = new Memento();

    // --------------------------------------------------------------------------------------------
    private static final class Memento {
        private static final String BUNDLE_KEY_FLAG_IS_FINISHED_POSTING = "bundle_key_flag_is_finished_posting" + PrID;;
        private static final String BUNDLE_KEY_FLAG_POSTED_WITH_CANCEL = "bundle_key_flag_posted_with_cancel" + PrID;;
        private static final String BUNDLE_KEY_FLAG_POSTED_WITH_FAILURE = "bundle_key_flag_posted_with_failure" + PrID;;
        private static final String BUNDLE_KEY_FLAG_POSTED_WITH_SUCCESS = "bundle_key_flag_posted_with_success" + PrID;;
        private static final String BUNDLE_KEY_FLAG_TOTAL_FOR_POSTING = "bundle_key_flag_total_for_posting" + PrID;;
        private static final String BUNDLE_KEY_STORED_REPORTS_ID = "bundle_key_stored_reports_id_" + PrID;
        private static final String BUNDLE_KEY_CURRENT_POST = "bundle_key_current_post_" + PrID;

        @InteractiveMode boolean isFinishedPosting;
        @InteractiveMode int postedWithCancel = 0;
        @InteractiveMode int postedWithFailure = 0;
        @InteractiveMode int postedWithSuccess = 0;
        @InteractiveMode int totalForPosting = 0;
        @InteractiveMode long storedReportsId = Constant.BAD_ID;

        private Post currentPost;

        @DebugLog
        private void toBundle(Bundle outState) {
            outState.putBoolean(BUNDLE_KEY_FLAG_IS_FINISHED_POSTING, isFinishedPosting);
            outState.putInt(BUNDLE_KEY_FLAG_POSTED_WITH_CANCEL, postedWithCancel);
            outState.putInt(BUNDLE_KEY_FLAG_POSTED_WITH_FAILURE, postedWithFailure);
            outState.putInt(BUNDLE_KEY_FLAG_POSTED_WITH_SUCCESS, postedWithSuccess);
            outState.putInt(BUNDLE_KEY_FLAG_TOTAL_FOR_POSTING, totalForPosting);
            outState.putLong(BUNDLE_KEY_STORED_REPORTS_ID, storedReportsId);
            outState.putParcelable(BUNDLE_KEY_CURRENT_POST, currentPost);
        }

        @DebugLog
        private static Memento fromBundle(Bundle savedInstanceState) {
            Memento memento = new Memento();
            memento.isFinishedPosting = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_IS_FINISHED_POSTING, false);
            memento.postedWithCancel = savedInstanceState.getInt(BUNDLE_KEY_FLAG_POSTED_WITH_CANCEL, 0);
            memento.postedWithFailure = savedInstanceState.getInt(BUNDLE_KEY_FLAG_POSTED_WITH_FAILURE, 0);
            memento.postedWithSuccess = savedInstanceState.getInt(BUNDLE_KEY_FLAG_POSTED_WITH_SUCCESS, 0);
            memento.totalForPosting = savedInstanceState.getInt(BUNDLE_KEY_FLAG_TOTAL_FOR_POSTING, 0);
            memento.storedReportsId = savedInstanceState.getLong(BUNDLE_KEY_STORED_REPORTS_ID, Constant.BAD_ID);
            memento.currentPost = savedInstanceState.getParcelable(BUNDLE_KEY_CURRENT_POST);
            return memento;
        }
    }

    // --------------------------------------------------------------------------------------------
    @Inject
    ReportPresenter(GetGroupReportBundleById getGroupReportBundleByIdUseCase, GetPostById getPostByIdUseCase,
                    DumpGroupReports dumpGroupReportsUseCase, PutGroupReportBundle putGroupReportBundleUseCase,
                    VkontakteEndpoint vkontakteEndpoint,
                    GroupReportToVoMapper groupReportToVoMapper, GroupReportEssenceMapper groupReportEssenceMapper,
                    GroupReportEssenceToVoMapper groupReportEssenceToVoMapper, PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.listAdapter = createListAdapter();
        this.getGroupReportBundleByIdUseCase = getGroupReportBundleByIdUseCase;
        this.getGroupReportBundleByIdUseCase.setPostExecuteCallback(createGetGroupReportBundleByIdCallback());
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.dumpGroupReportsUseCase = dumpGroupReportsUseCase;
        this.dumpGroupReportsUseCase.setPostExecuteCallback(createDumpGroupReportsCallback());
        this.putGroupReportBundleUseCase = putGroupReportBundleUseCase;  // no callback - background task
        this.vkontakteEndpoint = vkontakteEndpoint;
        this.groupReportToVoMapper = groupReportToVoMapper;
        this.groupReportEssenceMapper = groupReportEssenceMapper;
        this.groupReportEssenceToVoMapper = groupReportEssenceToVoMapper;
        this.postToSingleGridVoMapper = postToSingleGridVoMapper;
        this.postingProgressCallback = createPostingProgressCallback();
        this.postingCancelledCallback = createPostingCancelledCallback();
        this.postingFinishedCallback = createPostingFinishedCallback();
    }

    @Override
    protected BaseAdapter createListAdapter() {
        ReportAdapter adapter = new ReportAdapter();
        adapter.setOnItemClickListener((view, viewObject, position) -> {
            if (isViewAttached()) getView().openGroupDetailScreen(viewObject.groupId());
        });
        adapter.setOnErrorClickListener((view) -> retryLoadMore());
        return adapter;
    }

    @Override
    protected int getListTag() {
        return ReportFragment.RV_TAG;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppConfig.INSTANCE.useInteractiveReportScreen()) {
            Timber.d("Subscribe on posting progress callback on ReportScreen");
            ContentUtility.InMemoryStorage.setProgressCallback(postingProgressCallback);  // subscribe to progress updates
            ContentUtility.InMemoryStorage.setCancelCallback(postingCancelledCallback);   // subscribe to cancellation
            ContentUtility.InMemoryStorage.setFinishCallback(postingFinishedCallback);  // subscribe to finish posting
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (AppConfig.INSTANCE.useInteractiveReportScreen()) {
            // get id reserved for the item to store in repository next
            memento.storedReportsId = putGroupReportBundleUseCase.getReservedId();
            // put everything available in 'storedReports' to repository
            List<GroupReportEssence> essences = groupReportEssenceMapper.mapBack(storedReports);  // 'id' and 'timestamp' are ignored
            PutGroupReportBundle.Parameters parameters = new PutGroupReportBundle.Parameters(essences);
            putGroupReportBundleUseCase.setParameters(parameters);
            putGroupReportBundleUseCase.execute();
        }
        memento.toBundle(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (AppConfig.INSTANCE.useInteractiveReportScreen()) {
            Timber.d("Unsubscribe from posting progress callback on ReportScreen");
            ContentUtility.InMemoryStorage.setProgressCallback(null);  // unsubscribe from progress updates
            ContentUtility.InMemoryStorage.setCancelCallback(null);    // unsubscribe from cancellation
            ContentUtility.InMemoryStorage.setFinishCallback(null);  // unsubscribe from finish posting
        }
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onCloseView() {
        Timber.i("onCloseView");
        if (isViewAttached()) {
            if (AppConfig.INSTANCE.useInteractiveReportScreen() && !memento.isFinishedPosting) {
                getView().openCloseWhilePostingDialog();
            } else {
                getView().closeView();
            }
        }
    }

    /**
     * @Interactive_mode
     * {@link GroupReport} instances are incoming over time and stored in {@link ReportPresenter#storedReports}
     * collection. This collection will be dumped into file when wall posting process finishes.
     *
     * @Standard_mode
     * {@link GroupReportBundle} has already been recorded to repository and fetched as ReportScreen
     * is shown. It will be dumped directly from repository to file at any moment when the user
     * clicks the button.
     */
    @Override
    public void onDumpPressed() {
        Timber.i("onDumpPressed");
        boolean notReady = true;
        if (AppConfig.INSTANCE.useInteractiveReportScreen()) {
            if (memento.isFinishedPosting && !storedReports.isEmpty()) {
                dumpGroupReportsUseCase.setParameters(new DumpGroupReports.Parameters(storedReports));
                notReady = false;
            }
        } else {
            long groupReportBundleId = getGroupReportBundleByIdUseCase.getGroupReportId();
            if (groupReportBundleId != Constant.BAD_ID) {
                Timber.d("GroupReportBundle id [%s] is valid, ready to dump", groupReportBundleId);
                dumpGroupReportsUseCase.setParameters(new DumpGroupReports.Parameters(groupReportBundleId));
                notReady = false;
            }
        }

        if (notReady) {
            Timber.d("GroupReportBundle is not available to dump");
            if (isViewAttached()) getView().openDumpNotReadyDialog();
        } else if (isViewAttached()) {
            getView().openEditDumpFileNameDialog();
        }
    }

    @InteractiveMode @Override
    public void interruptPostingAndClose(boolean shouldClose) {
        Timber.i("interruptPostingAndClose: %s", shouldClose);
        if (memento.isFinishedPosting) return;  // no-op if no posting is in progress

        if (AppConfig.INSTANCE.useInteractiveReportScreen()) {
            /**
             * Setting this flag we disable warning popup on back pressed. But this will be set in
             * anyway in {@link ReportPresenter#createPostingFinishedCallback()} callback.
             */
            memento.isFinishedPosting = true;

            ApplicationComponent component = getApplicationComponent();
            if (component != null) component.threadExecutor().shutdownNow();
        }

        if (shouldClose && isViewAttached()) getView().closeView();
    }

    @Override
    public void performDumping(String path) {
        Timber.i("performDumping: %s", path);
        dumpGroupReportsUseCase.setPath(path);
        dumpGroupReportsUseCase.execute();
    }

    @Override
    public void performReverting() {
        Timber.i("performReverting");
        if (isViewAttached()) getView().onPostRevertingStarted();
        vkontakteEndpoint.deleteWallPosts(storedReports, createDeleteWallPostsCallback());
    }

    @Override
    public void retry() {
        Timber.i("retry");
        memento.postedWithSuccess = 0;  // drop counter
        memento.postedWithFailure = 0;  // drop counter
        memento.isFinishedPosting = false;
        memento.currentPost = null;
        storedReports.clear();
        listAdapter.clear();
        dropListStat();
        freshStart();
    }

    @Override
    public void retryPost() {
        Timber.i("retryPost");
        getPostByIdUseCase.execute();
    }

    /* List */
    // ------------------------------------------
    @Override
    protected void onLoadMore() {
        // TODO: on load more
    }

    private void retryLoadMore() {
        listAdapter.onError(false); // show loading more
        // TODO: load more limit-offset
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        if (isViewAttached()) getView().showLoading(getListTag());
        if (!AppConfig.INSTANCE.useInteractiveReportScreen()) {
            getGroupReportBundleByIdUseCase.execute();
        } else if (isViewAttached()) {
            // disable swipe-to-refresh when GroupReport-s are coming interactively
            getView().enableSwipeToRefresh(false);
        }
        getPostByIdUseCase.execute();
    }

    @Override
    protected void onRestoreState() {
        memento = Memento.fromBundle(savedInstanceState);
        if (AppConfig.INSTANCE.useInteractiveReportScreen()) {
            /**
             * Restore Post in interactive mode. In standard mode {@link ReportPresenter#freshStart()}
             * will be invoked and Post will be re-loaded from repository. So, this line is placed here
             * inside if-statement in order not to re-load Post twice.
             */
            applyPost(memento.currentPost);  // restore Post

            // restore all those GroupReport-s from repository that we had managed to store.
            memento.isFinishedPosting = true;  // assume posting has finished on state restore
            getGroupReportBundleByIdUseCase.setGroupReportId(memento.storedReportsId);
            getGroupReportBundleByIdUseCase.execute();
        } else {
            freshStart();  // nothing to be restored
        }
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private UseCase.OnPostExecuteCallback<GroupReportBundle> createGetGroupReportBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<GroupReportBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable GroupReportBundle bundle) {
                long groupReportBundleId = getGroupReportBundleByIdUseCase.getGroupReportId();
                if (bundle == null || bundle.groupReports() == null) {
                    Timber.e("GroupReportBundle wasn't found by id [%s], or groupReports property is null", groupReportBundleId);
                    throw new ProgramException();
                } else if (bundle.groupReports().isEmpty()) {
                    Timber.i("Use-Case: succeeded to get GroupReportBundle by id");
                    if (isViewAttached()) getView().showEmptyList(getListTag());
                } else {
                    Timber.i("Use-Case: succeeded to get GroupReportBundle by id");
                    int[] counters = bundle.statusCount();
                    List<ReportListItemVO> vos = groupReportToVoMapper.map(bundle.groupReports());
                    if (AppConfig.INSTANCE.useInteractiveReportScreen()) {
                        /**
                         * In interactive mode items are incoming from the oldest to the recents,
                         * this order is reverse to the order these items are stored in GroupReportBundle.
                         *
                         * We can reach this place in interactive mode only when ReportScreen is restored
                         * and hence fetched all it's data from repository, where order is direct.
                         * So, in order to keep user experience, we must populate list with restored
                         * items in reversed order.
                         */
                        listAdapter.populateInverse(vos, false);  // items was restored from repository in interactive mode
                    } else {
                        listAdapter.populate(vos, false);
                    }
                    if (isViewAttached()) {
                        getView().showGroupReports(vos == null || vos.isEmpty());
                        getView().updatePostedCounters(counters[GroupReport.STATUS_SUCCESS], bundle.groupReports().size());
                    }

                    /**
                     * Populate {@link ReportPresenter#storedReports} when get GroupReportBundle
                     * use-case has finished with data inside. In interactive mode, this is only
                     * possible when the entire ReportScreen is restored after destruction, i.e.
                     * {@link ReportPresenter#onRestoreState()} is the only place where
                     * {@link GetGroupReportBundleById} use-case is executed.
                     */
                    storedReports.clear();
                    storedReports.addAll(bundle.groupReports());
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get GroupReportBundle by id");
                if (isViewAttached()) getView().showError(getListTag());
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @DebugLog @Override
            public void onFinish(@Nullable Post post) {
                Timber.i("Use-Case: succeeded to get Post by id");
                memento.currentPost = post;
                applyPost(post);
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get Post by id");
                if (isViewAttached()) getView().showErrorPost();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<String> createDumpGroupReportsCallback() {
        return new UseCase.OnPostExecuteCallback<String>() {
            @Override
            public void onFinish(@Nullable String path) {
                if (!TextUtils.isEmpty(path)) {
                    Timber.i("Use-Case: succeeded to dump GroupReport-s");
                    if (isViewAttached()) getView().showDumpSuccess(path);
                } else {
                    Timber.e("Use-Case: failed to dump GroupReport-s");
                    if (isViewAttached()) getView().showDumpError();
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to dump GroupReport-s");
                if (isViewAttached()) getView().showDumpError();
            }
        };
    }

    // ------------------------------------------
    private UseCase.OnPostExecuteCallback<Boolean> createDeleteWallPostsCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @Override
            public void onFinish(@Nullable Boolean result) {
                Timber.i("Use-Case: succeeded to delete wall Post-s");
                if (isViewAttached()) getView().onPostRevertingFinished();
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to delete wall Post-s");
                if (isViewAttached()) getView().onPostRevertingError();
            }
        };
    }

    // ------------------------------------------
    @InteractiveMode @SuppressWarnings("unchecked")
    private MultiUseCase.ProgressCallback<GroupReportEssence> createPostingProgressCallback() {
        return (index, total, item) -> {
            Timber.v("Posting progress: %s / %s", index + 1, total);
            MakeWallPost.Parameters params = (MakeWallPost.Parameters) item.parameters;
            Group group = params.getGroup();  // null parameters are impossible because this is checked inside the use-case
            Timber.v("%s", group.toString());
            // TODO: use terminal error from proper UseCase instead of hardcoded one
            GroupReportEssence model = VkontakteEndpoint.refineModel(item, group, Api5VkUseCaseException.class, Api220VkUseCaseException.class);
            if (item.data != null)  ++memento.postedWithSuccess;  // count successful posting
            if (item.error != null) ++memento.postedWithFailure;  // count failed posting
            /**
             * Flag {@link Ordered#cancelled} is not checked here because it could be true and
             * {@link Ordered#data} or {@link Ordered#error} could not be null at the same time.
             */
            if (item.data == null && item.error == null) ++memento.postedWithCancel;  // count cancelled posting

            ReportListItemVO viewObject = groupReportEssenceToVoMapper.map(model);
            listAdapter.addInverse(viewObject);  // add items on top of the list
            if (isViewAttached()) {
                getView().showGroupReports(false);  // idempotent call (no-op if list items are already visible)
                getView().updatePostedCounters(memento.postedWithSuccess, total);
                getView().getListView(getListTag()).smoothScrollToPosition(0);  // scroll on top of the list as items are incoming
                // TODO: estimate time to complete posting, use DomainConfig.INSTANCE.multiUseCaseSleepInterval
            }

            long timestamp = System.currentTimeMillis();
            groupReportEssenceMapper.setGroupReportId(Constant.INIT_ID);  // fictive id
            groupReportEssenceMapper.setTimestamp(timestamp);
            storedReports.add(groupReportEssenceMapper.map(model));

            Timber.v("Posting stat: success [%s], failure [%s], cancel [%s], total [%s]",
                    memento.postedWithSuccess, memento.postedWithFailure, memento.postedWithCancel, total);
            // TODO: not properly counted if there are retry-failed use-cases
            if (!memento.isFinishedPosting) memento.isFinishedPosting = memento.postedWithCancel + memento.postedWithFailure + memento.postedWithSuccess == total;
            memento.totalForPosting = total;  // save counter to use further
        };
    }

    @DebugLog @InteractiveMode
    private MultiUseCase.CancelCallback createPostingCancelledCallback() {
        return (reason) -> {
            Timber.i("Posting has been cancelled");
            memento.isFinishedPosting = true;
            if (isViewAttached()) {
                if (EndpointUtility.hasAccessTokenExhausted(reason)) {
                    Timber.w("Access Token has exhausted !");
                    getView().onAccessTokenExhausted();
                } else {
                    getView().onPostingCancel();
                }
            }
        };
    }

    @DebugLog @InteractiveMode
    private MultiUseCase.FinishCallback createPostingFinishedCallback() {
        return () -> {
            Timber.i("Posting has been finished");
            memento.isFinishedPosting = true;
            if (isViewAttached()) getView().onPostingFinished(memento.postedWithSuccess, memento.totalForPosting);
        };
    }

    /* Utility */
    // --------------------------------------------------------------------------------------------
    private void applyPost(Post post) {
        if (isViewAttached()) {
            if (post != null) {
                getView().showPost(postToSingleGridVoMapper.map(post));
            } else {
                getView().showEmptyPost();
            }
        }
    }
}
