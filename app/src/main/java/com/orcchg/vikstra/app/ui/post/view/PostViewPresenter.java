package com.orcchg.vikstra.app.ui.post.view;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.post.create.PostCreateActivity;
import com.orcchg.vikstra.app.ui.viewobject.PostViewVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToVoMapper;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.model.Post;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class PostViewPresenter extends BasePresenter<PostViewContract.View> implements PostViewContract.Presenter {

    private final GetPostById getPostByIdUseCase;
    private final PostToVoMapper postToVoMapper;

    @Inject
    PostViewPresenter(GetPostById getPostByIdUseCase, PostToVoMapper postToVoMapper) {
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.postToVoMapper = postToVoMapper;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PostCreateActivity.REQUEST_CODE:  // post could change on PostCreateScreen
                if (resultCode == Activity.RESULT_OK) {
                    Timber.d("Post has been changed on PostViewScreen resulting from screen with request code: %s", requestCode);
                    retry();  // refresh post
                }
                break;
        }
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void retry() {
        Timber.i("retry");
        freshStart();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        if (isViewAttached()) getView().showLoading();
        getPostByIdUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @Override
            public void onFinish(@Nullable Post post) {
                if (post == null) {
                    Timber.wtf("Post wasn't found by id: %s", getPostByIdUseCase.getPostId());
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get Post by id");
                PostViewVO viewObject = postToVoMapper.map(post);
                if (isViewAttached()) getView().showPost(viewObject);
                // TODO: if updating existing post - fill text field and media attachment view container
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get Post by id");
                if (isViewAttached()) getView().showError();
            }
        };
    }
}
