package com.orcchg.vikstra.app.ui.post.list;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridAdapter;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.post.GetPosts;
import com.orcchg.vikstra.domain.model.Post;

import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class PostListPresenter extends BaseListPresenter<PostListContract.View>
        implements PostListContract.Presenter {

    private final GetPosts getPostsUseCase;

    private final @BaseSelectAdapter.SelectMode int selectMode;

    private final PostToSingleGridVoMapper postToSingleGridVoMapper;

    @Inject
    public PostListPresenter(@BaseSelectAdapter.SelectMode int selectMode,
             GetPosts getPostsUseCase, PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.selectMode = selectMode;
        this.listAdapter = createListAdapter();
        this.getPostsUseCase = getPostsUseCase;
        this.getPostsUseCase.setPostExecuteCallback(createGetPostsCallback());
        this.postToSingleGridVoMapper = postToSingleGridVoMapper;
    }

    @Override
    protected BaseAdapter createListAdapter() {
        PostSingleGridAdapter adapter = new PostSingleGridAdapter(selectMode, false);
        adapter.setOnItemClickListener((view, viewObject, position) -> {
            if (isViewAttached()) getView().openPostViewScreen(viewObject.id());
        });
        adapter.setOnErrorClickListener((view) -> retryLoadMore());
        return adapter;
    }

    @Override
    protected int getListTag() {
        return PostListFragment.RV_TAG;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void retry() {
        Timber.i("retry");
        listAdapter.clear();
        dropListStat();
        freshStart();
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
        getPostsUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<List<Post>> createGetPostsCallback() {
        return new UseCase.OnPostExecuteCallback<List<Post>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<Post> posts) {
                if (posts == null) {
                    Timber.e("List of Post items must not be null, it could be empty at least");
                    throw new ProgramException();
                } else if (posts.isEmpty()) {
                    Timber.i("Use-Case: succeeded to get list of Post-s");
                    if (isViewAttached()) getView().showEmptyList(getListTag());
                } else {
                    Timber.i("Use-Case: succeeded to get list of Post-s");
                    memento.currentSize += posts.size();
                    List<PostSingleGridItemVO> vos = postToSingleGridVoMapper.map(posts);
                    listAdapter.populate(vos, false);
                    if (isViewAttached()) getView().showPosts(vos == null || vos.isEmpty());
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get list of Post-s");
                if (memento.currentSize <= 0) {
                    if (isViewAttached()) getView().showError(getListTag());
                } else {
                    listAdapter.onError(true);
                }
            }
        };
    }

    // TODO: assign totalItems
}
