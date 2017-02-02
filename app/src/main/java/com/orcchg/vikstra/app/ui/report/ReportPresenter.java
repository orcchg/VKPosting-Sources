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
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.Ordered;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.report.DumpGroupReports;
import com.orcchg.vikstra.domain.interactor.report.GetGroupReportBundleById;
import com.orcchg.vikstra.domain.interactor.vkontakte.MakeWallPost;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.model.essense.mapper.GroupReportEssenceMapper;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class ReportPresenter extends BaseListPresenter<ReportContract.View> implements ReportContract.Presenter {

    private final GetGroupReportBundleById getGroupReportBundleByIdUseCase;
    private final GetPostById getPostByIdUseCase;
    private final DumpGroupReports dumpGroupReportsUseCase;

    private final GroupReportToVoMapper groupReportToVoMapper;
    private final GroupReportEssenceToVoMapper groupReportEssenceToVoMapper;
    private final PostToSingleGridVoMapper postToSingleGridVoMapper;

    // used in interactive mode {@link InteractiveMode}
    private final MultiUseCase.ProgressCallback<GroupReportEssence> postingProgressCallback;
    private final MultiUseCase.CancelCallback postingCancelledCallback;
    private final MultiUseCase.FinishCallback postingFinishedCallback;
    private @InteractiveMode List<GroupReport> storedReports = new ArrayList<>();
    private @InteractiveMode boolean isFinishedPosting;
    private @InteractiveMode int postedWithCancel = 0;
    private @InteractiveMode int postedWithFailure = 0;
    private @InteractiveMode int postedWithSuccess = 0;
    private @InteractiveMode int totalForPosting = 0;

    @Inject
    ReportPresenter(GetGroupReportBundleById getGroupReportBundleByIdUseCase, GetPostById getPostByIdUseCase,
                    DumpGroupReports dumpGroupReportsUseCase, GroupReportToVoMapper groupReportToVoMapper,
                    GroupReportEssenceToVoMapper groupReportEssenceToVoMapper,
                    PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.listAdapter = createListAdapter();
        this.getGroupReportBundleByIdUseCase = getGroupReportBundleByIdUseCase;
        this.getGroupReportBundleByIdUseCase.setPostExecuteCallback(createGetGroupReportBundleByIdCallback());
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.dumpGroupReportsUseCase = dumpGroupReportsUseCase;
        this.dumpGroupReportsUseCase.setPostExecuteCallback(createDumpGroupReportsCallback());
        this.groupReportToVoMapper = groupReportToVoMapper;
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
            if (AppConfig.INSTANCE.useInteractiveReportScreen() && !isFinishedPosting) {
                getView().openCloseWhilePostingDialog();
            } else {
                getView().closeView();
            }
        }
    }

    @Override
    public void onDumpPressed() {
        Timber.i("onDumpPressed");
        boolean notReady = true;
        if (AppConfig.INSTANCE.useInteractiveReportScreen()) {
            if (isFinishedPosting && !storedReports.isEmpty()) {
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
        if (isFinishedPosting) return;  // no-op if no posting is in progress

        if (AppConfig.INSTANCE.useInteractiveReportScreen()) {
            /**
             * Setting this flag we disable warning popup on back pressed. But this will be set in
             * anyway in {@link ReportPresenter#createPostingFinishedCallback()} callback.
             */
            isFinishedPosting = true;

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
    public void retry() {
        Timber.i("retry");
        postedWithSuccess = 0;  // drop counter
        postedWithFailure = 0;  // drop counter
        isFinishedPosting = false;
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

    /* Callback */
    // --------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private UseCase.OnPostExecuteCallback<GroupReportBundle> createGetGroupReportBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<GroupReportBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable GroupReportBundle bundle) {
                if (bundle == null || bundle.groupReports() == null) {
                    Timber.e("GroupReportBundle wasn't found by id [%s], or groupReports property is null",
                            getGroupReportBundleByIdUseCase.getGroupReportId());
                    throw new ProgramException();
                } else if (bundle.groupReports().isEmpty()) {
                    Timber.i("Use-Case: succeeded to get GroupReportBundle by id");
                    if (isViewAttached()) getView().showEmptyList(getListTag());
                } else {
                    Timber.i("Use-Case: succeeded to get GroupReportBundle by id");
                    int[] counters = bundle.statusCount();
                    List<ReportListItemVO> vos = groupReportToVoMapper.map(bundle.groupReports());
                    listAdapter.populate(vos, false);
                    if (isViewAttached()) {
                        getView().showGroupReports(vos == null || vos.isEmpty());
                        getView().updatePostedCounters(counters[GroupReport.STATUS_SUCCESS], bundle.groupReports().size());
                    }
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
                if (isViewAttached()) {
                    if (post != null) {
                        getView().showPost(postToSingleGridVoMapper.map(post));
                    } else {
                        getView().showEmptyPost();
                    }
                }
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
    @InteractiveMode @SuppressWarnings("unchecked")
    private MultiUseCase.ProgressCallback<GroupReportEssence> createPostingProgressCallback() {
        return (index, total, item) -> {
            Timber.v("Posting progress: %s / %s", index + 1, total);
            MakeWallPost.Parameters params = (MakeWallPost.Parameters) item.parameters;
            Group group = params.getGroup();  // null parameters are impossible because this is checked inside the use-case
            Timber.v("%s", group.toString());
            // TODO: use terminal error from proper UseCase instead of hardcoded one
            GroupReportEssence model = VkontakteEndpoint.refineModel(item, group, Api220VkUseCaseException.class);
            if (item.data != null)  ++postedWithSuccess;  // count successful posting
            if (item.error != null) ++postedWithFailure;  // count failed posting
            /**
             * Flag {@link Ordered#cancelled} is not checked here because it could be true and
             * {@link Ordered#data} or {@link Ordered#error} could not be null at the same time.
             */
            if (item.data == null && item.error == null) ++postedWithCancel;  // count cancelled posting

            ReportListItemVO viewObject = groupReportEssenceToVoMapper.map(model);
            listAdapter.addInverse(viewObject);
            if (isViewAttached()) {
                getView().showGroupReports(false);  // idempotent call (no-op if list items are already visible)
                getView().updatePostedCounters(postedWithSuccess, total);
                getView().getListView(getListTag()).smoothScrollToPosition(0);
                // TODO: estimate time to complete posting, use DomainConfig.INSTANCE.multiUseCaseSleepInterval
            }

            long timestamp = System.currentTimeMillis();
            GroupReportEssenceMapper mapper = new GroupReportEssenceMapper(Constant.INIT_ID, timestamp);  // fictive id
            storedReports.add(mapper.map(model));

            Timber.v("Posting stat: success [%s], failure [%s], cancel [%s], total [%s]",
                    postedWithSuccess, postedWithFailure, postedWithCancel, total);
            // TODO: not properly counted if there are retry-failed use-cases
            if (!isFinishedPosting) isFinishedPosting = postedWithCancel + postedWithFailure + postedWithSuccess == total;
            totalForPosting = total;  // save counter to use further
        };
    }

    @DebugLog @InteractiveMode
    private MultiUseCase.CancelCallback createPostingCancelledCallback() {
        return () -> {
            Timber.i("Posting has been finished");
            if (isViewAttached()) getView().onPostingCancel();
            isFinishedPosting = true;
        };
    }

    @DebugLog @InteractiveMode
    private MultiUseCase.FinishCallback createPostingFinishedCallback() {
        return () -> {
            Timber.i("Posting has finished");
            if (isViewAttached()) getView().onPostingFinished(postedWithSuccess, totalForPosting);
            isFinishedPosting = true;
        };
    }
}
