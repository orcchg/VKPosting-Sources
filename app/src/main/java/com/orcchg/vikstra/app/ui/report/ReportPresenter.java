package com.orcchg.vikstra.app.ui.report;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.model.Post;

import javax.inject.Inject;

public class ReportPresenter extends BasePresenter<ReportContract.View> implements ReportContract.Presenter {

    private final GetPostById getPostByIdUseCase;

    private final PostToSingleGridVoMapper postToSingleGridVoMapper;

    @Inject
    ReportPresenter(GetPostById getPostByIdUseCase, PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.postToSingleGridVoMapper = postToSingleGridVoMapper;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        getPostByIdUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @Override
            public void onFinish(@Nullable Post post) {
                if (isViewAttached()) getView().showPost(postToSingleGridVoMapper.map(post));
            }

            @Override
            public void onError(Throwable e) {
                // TODO: impl
            }
        };
    }
}
