package com.orcchg.vikstra.app.ui.post.single;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.util.ValueEmitter;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.post.GetPosts;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class PostSingleGridPresenter extends BaseListPresenter<PostSingleGridContract.View>
        implements PostSingleGridContract.Presenter {

    private final GetPosts getPostsUseCase;

    private long selectedPostId = Constant.BAD_ID;
    private final @BaseSelectAdapter.SelectMode int selectMode;
    private ValueEmitter<Boolean> externalValueEmitter;

    private final PostToSingleGridVoMapper postToSingleGridVoMapper;

    @Inject
    public PostSingleGridPresenter(@BaseSelectAdapter.SelectMode int selectMode,
           GetPosts getPostsUseCase, PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.selectMode = selectMode;
        this.listAdapter = createListAdapter();
        this.getPostsUseCase = getPostsUseCase;
        this.getPostsUseCase.setPostExecuteCallback(createGetPostsCallback());
        this.postToSingleGridVoMapper = postToSingleGridVoMapper;
    }

    public void setExternalValueEmitter(ValueEmitter<Boolean> listener) {
        externalValueEmitter = listener;
    }

    @Override
    protected BaseAdapter createListAdapter() {
        PostSingleGridAdapter adapter = new PostSingleGridAdapter(selectMode, true);
        adapter.setOnItemClickListener((view, viewObject, position) ->
            changeSelectedPostId(viewObject.getSelection() ? viewObject.id() : Constant.BAD_ID));
        adapter.setOnItemLongClickListener((view, viewObject, position) -> {
            if (isViewAttached()) getView().openPostViewScreen(viewObject.id());
        });
        adapter.setOnNewItemClickListener((view, viewObject, position) -> {
            if (isViewAttached()) getView().openPostCreateScreen();
        });
        adapter.setOnErrorClickListener((view) -> retryLoadMore());
        return adapter;
    }

    @Override
    protected int getListTag() {
        return PostSingleGridFragment.RV_TAG;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void retry() {
        Timber.i("retry");
        changeSelectedPostId(Constant.BAD_ID);  // drop selection
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
    @DebugLog
    public long getSelectedPostId() {
        return selectedPostId;
    }

    @Override
    protected void freshStart() {
        if (isViewAttached()) getView().showLoading(getListTag());
        getPostsUseCase.execute();
    }

    @DebugLog
    private  boolean changeSelectedPostId(long newId) {
        selectedPostId = newId;
        if (externalValueEmitter != null) {
            externalValueEmitter.emit(newId != Constant.BAD_ID);
            return true;
        }
        return false;
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<List<Post>> createGetPostsCallback() {
        return new UseCase.OnPostExecuteCallback<List<Post>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<Post> posts) {
                if (posts == null) {
                    Timber.e("List of Post-s must not be null, it could be empty at least");
                    throw new ProgramException();
                } else if (posts.isEmpty()) {
                    Timber.i("Use-Case: succeeded to get list of Post-s");
                    if (isViewAttached()) getView().showEmptyList(getListTag());
                } else {
                    Timber.i("Use-Case: succeeded to get list of Post-s");
                    List<PostSingleGridItemVO> vos = postToSingleGridVoMapper.map(posts);
                    listAdapter.populate(vos, false);
                    if (isViewAttached()) getView().showPosts(vos == null || vos.isEmpty());
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get list of Post-s");
                if (isViewAttached()) getView().showError(getListTag());
            }
        };
    }
}
