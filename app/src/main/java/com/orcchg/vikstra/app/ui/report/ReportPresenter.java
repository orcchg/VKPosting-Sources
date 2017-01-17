package com.orcchg.vikstra.app.ui.report;

import android.os.Bundle;
import android.support.annotation.Nullable;

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
import com.orcchg.vikstra.domain.interactor.report.GetGroupReportBundleById;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class ReportPresenter extends BaseListPresenter<ReportContract.View> implements ReportContract.Presenter {

    private final GetGroupReportBundleById getGroupReportBundleByIdUseCase;
    private final GetPostById getPostByIdUseCase;

    private final GroupReportToVoMapper groupReportToVoMapper;
    private final GroupReportEssenceToVoMapper groupReportEssenceToVoMapper;
    private final PostToSingleGridVoMapper postToSingleGridVoMapper;

    private final MultiUseCase.ProgressCallback<GroupReportEssence> postingProgressCallback;  // used in interactive mode
    private int posted = 0;  // used in interactive mode

    @Inject
    ReportPresenter(GetGroupReportBundleById getGroupReportBundleByIdUseCase, GetPostById getPostByIdUseCase,
                    GroupReportToVoMapper groupReportToVoMapper, GroupReportEssenceToVoMapper groupReportEssenceToVoMapper,
                    PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.listAdapter = createListAdapter();
        this.getGroupReportBundleByIdUseCase = getGroupReportBundleByIdUseCase;
        this.getGroupReportBundleByIdUseCase.setPostExecuteCallback(createGetGroupReportBundleByIdCallback());
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
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
    public void onScroll(int itemsLeftToEnd) {
        // TODO: load more
    }

    @Override
    public void retry() {
        Timber.i("retry");
        listAdapter.clear();
        dropListStat();
        freshStart();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        posted = 0;  // drop counter
        if (isViewAttached()) getView().showLoading(ReportFragment.RV_TAG);
        if (!AppConfig.INSTANCE.useInteractiveReportScreen()) {
            getGroupReportBundleByIdUseCase.execute();
        }
        getPostByIdUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
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
                // TODO: failed to load post
                Timber.e("Use-Case: failed to get Post by id");
                if (isViewAttached()) getView().showError(ReportFragment.RV_TAG);
            }
        };
    }

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
                    if (isViewAttached()) getView().showEmptyList(ReportFragment.RV_TAG);
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
                if (isViewAttached()) getView().showError(ReportFragment.RV_TAG);
            }
        };
    }

    // ------------------------------------------
    private MultiUseCase.ProgressCallback<GroupReportEssence> createPostingProgressCallback() {
        return (index, total, item) -> {
            // unwrap data
            GroupReportEssence model = null;
            if (item.data != null) {
                model = item.data;
                ++posted;  // count successful posting
            }
            if (item.error != null) {
                VkUseCaseException e = (VkUseCaseException) item.error;
                Group group = ContentUtility.InMemoryStorage.getSelectedGroupsForPosting().get(index);
                model = GroupReportEssence.builder()
                        .setErrorCode(e.getErrorCode())
                        .setGroup(group)
                        .setWallPostId(Constant.BAD_ID)
                        .build();
            }
            if (model == null) {
                Timber.wtf("Unreachable state: GroupReportEssence must always be constructed from Ordered<> item");
                throw new ProgramException();
            }

            ReportListItemVO viewObject = groupReportEssenceToVoMapper.map(model);
            listAdapter.addInverse(viewObject);
            if (isViewAttached()) {
                getView().showGroupReports(false);  // idemponent call (no-op if list items are already visible)
                getView().updatePostedCounters(posted, total);
                // TODO: estimate time to complete posting, use DomainConfig.INSTANCE.multiUseCaseSleepInterval
            }
        };
    }
}
