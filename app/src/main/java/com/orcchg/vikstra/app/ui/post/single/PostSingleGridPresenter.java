package com.orcchg.vikstra.app.ui.post.single;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.post.GetPosts;
import com.orcchg.vikstra.domain.model.Post;

import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class PostSingleGridPresenter extends BaseListPresenter<PostSingleGridContract.View>
        implements PostSingleGridContract.Presenter {

    private final GetPosts getPostsUseCase;

    final PostToSingleGridVoMapper postToSingleGridVoMapper;

    @Inject
    PostSingleGridPresenter(GetPosts getPostsUseCase, PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.listAdapter = createListAdapter();
        this.getPostsUseCase = getPostsUseCase;
        this.getPostsUseCase.setPostExecuteCallback(createGetPostsCallback());
        this.postToSingleGridVoMapper = postToSingleGridVoMapper;
    }

    @Override
    protected BaseAdapter createListAdapter() {
        PostSingleGridAdapter adapter = new PostSingleGridAdapter();
        adapter.setOnItemClickListener((view, viewObject, position) -> {
            if (isViewAttached()) getView().openPostViewScreen(viewObject.id());
        });
        adapter.setOnNewItemClickListener((view, viewObject, position) -> {
            if (isViewAttached()) getView().openPostCreateScreen();
        });
        return adapter;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void retry() {
        listAdapter.clear();
        freshStart();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        getPostsUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<List<Post>> createGetPostsCallback() {
        return new UseCase.OnPostExecuteCallback<List<Post>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<Post> values) {
                // TODO: NPE
                List<PostSingleGridItemVO> vos = postToSingleGridVoMapper.map(values);
                listAdapter.populate(vos, false);
                if (isViewAttached()) getView().showPosts(vos == null || vos.isEmpty());
            }

            @Override
            public void onError(Throwable e) {
                // TODO: impl
            }
        };
    }
}
