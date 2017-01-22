package com.orcchg.vikstra.app.ui.report;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orcchg.vikstra.app.AppConfig;
import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.viewobject.ReportListItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.GroupReportEssenceToVoMapper;
import com.orcchg.vikstra.app.ui.viewobject.mapper.GroupReportToVoMapper;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.data.source.memory.ContentUtility;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.exception.vkontakte.VkUseCaseException;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.report.DumpGroupReports;
import com.orcchg.vikstra.domain.interactor.report.GetGroupReportBundleById;
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

    // used in interactive mode
    private final MultiUseCase.ProgressCallback<GroupReportEssence> postingProgressCallback;
    private List<GroupReport> storedReports = new ArrayList<>();
    private boolean isFinishedPosting;
    private int postedWithSuccess = 0, postedWithFailure = 0;

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
    }

    @Override
    protected BaseAdapter createListAdapter() {
        ReportAdapter adapter = new ReportAdapter();
        adapter.setOnItemClickListener((view, viewObject, position) -> {
            // TODO: click
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
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (AppConfig.INSTANCE.useInteractiveReportScreen()) {
            Timber.d("Unsubscribe from posting progress callback on ReportScreen");
            ContentUtility.InMemoryStorage.setProgressCallback(null);  // unsubscribe from progress updates
        }
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
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

    @Override
    public void performDumping(String path) {
        Timber.i("performDumping: %s", path);
        dumpGroupReportsUseCase.setPath(path);
        dumpGroupReportsUseCase.execute();
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
                    Timber.wtf("GroupReportBundle wasn't found by id [%s], or groupReports property is null",
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
    @SuppressWarnings("unchecked")
    private MultiUseCase.ProgressCallback<GroupReportEssence> createPostingProgressCallback() {
        return (index, total, item) -> {
            // unwrap data
            GroupReportEssence model = null;
            if (item.data != null) {
                model = item.data;
                ++postedWithSuccess;  // count successful posting
            }
            if (item.error != null) {
                VkUseCaseException e = (VkUseCaseException) item.error;
                Group group = ContentUtility.InMemoryStorage.getSelectedGroupsForPosting().get(index);
                model = GroupReportEssence.builder()
                        .setErrorCode(e.getErrorCode())
                        .setGroup(group)
                        .setWallPostId(Constant.BAD_ID)
                        .build();
                ++postedWithFailure;  // count failed posting
            }
            if (model == null) {
                Timber.wtf("Unreachable state: GroupReportEssence must always be constructed from Ordered<> item");
                throw new ProgramException();
            }

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

            isFinishedPosting = postedWithSuccess + postedWithFailure == total;
        };
    }
}
