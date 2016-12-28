package com.orcchg.vikstra.app.ui.report;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.report.GetGroupReportBundleById;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.Post;

import javax.inject.Inject;

public class ReportPresenter extends BasePresenter<ReportContract.View> implements ReportContract.Presenter {

    private final GetGroupReportBundleById getGroupReportBundleByIdUseCase;
    private final GetPostById getPostByIdUseCase;

    private final PostToSingleGridVoMapper postToSingleGridVoMapper;

    @Inject
    ReportPresenter(GetGroupReportBundleById getGroupReportBundleByIdUseCase, GetPostById getPostByIdUseCase,
                    PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.getGroupReportBundleByIdUseCase = getGroupReportBundleByIdUseCase;
        this.getGroupReportBundleByIdUseCase.setPostExecuteCallback(createGetGroupReportBundleByIdCallback());
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.postToSingleGridVoMapper = postToSingleGridVoMapper;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onScroll(int itemsLeftToEnd) {
        // TODO: load more
    }

    @Override
    public void retry() {
        freshStart();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        getGroupReportBundleByIdUseCase.execute();
        getPostByIdUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @Override
            public void onFinish(@Nullable Post post) {
                // TODO: NPE - bad id
                if (isViewAttached()) getView().showPost(postToSingleGridVoMapper.map(post));
            }

            @Override
            public void onError(Throwable e) {
                // TODO: impl
            }
        };
    }

    private UseCase.OnPostExecuteCallback<GroupReportBundle> createGetGroupReportBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<GroupReportBundle>() {
            @Override
            public void onFinish(@Nullable GroupReportBundle values) {
                // TODO: NPE - bad id
                // TODO: show groups report
            }

            @Override
            public void onError(Throwable e) {
                // TODO: impl
            }
        };
    }
}
