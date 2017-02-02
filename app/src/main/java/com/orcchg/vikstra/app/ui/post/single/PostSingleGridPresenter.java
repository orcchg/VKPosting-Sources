package com.orcchg.vikstra.app.ui.post.single;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.post.OutConstants;
import com.orcchg.vikstra.app.ui.post.create.PostCreateActivity;
import com.orcchg.vikstra.app.ui.util.ValueEmitter;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.post.DeletePost;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.post.GetPosts;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class PostSingleGridPresenter extends BaseListPresenter<PostSingleGridContract.View>
        implements PostSingleGridContract.Presenter {

    private final GetPostById getPostByIdUseCase;
    private final GetPosts getPostsUseCase;
    private final DeletePost deletePostUseCase;

    private List<Post> posts = new ArrayList<>();
    private long selectedPostId = Constant.BAD_ID;
    protected final @BaseSelectAdapter.SelectMode int selectMode;
    private ValueEmitter<Boolean> externalValueEmitter;

    private final PostToSingleGridVoMapper postToSingleGridVoMapper;

    @Inject
    public PostSingleGridPresenter(@BaseSelectAdapter.SelectMode int selectMode, GetPostById getPostByIdUseCase,
                                   GetPosts getPostsUseCase, DeletePost deletePostUseCase,
                                   PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.selectMode = selectMode;
        this.listAdapter = createListAdapter();
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.getPostsUseCase = getPostsUseCase;
        this.getPostsUseCase.setPostExecuteCallback(createGetPostsCallback());
        this.deletePostUseCase = deletePostUseCase;  // no callback - background task
        this.postToSingleGridVoMapper = postToSingleGridVoMapper;
    }

    public void setExternalValueEmitter(ValueEmitter<Boolean> listener) {
        externalValueEmitter = listener;
    }

    @Override
    protected BaseAdapter createListAdapter() {
        PostSingleGridAdapter adapter = new PostSingleGridAdapter(selectMode, true);
        prepareAdapter(adapter);
        return adapter;
    }

    protected void prepareAdapter(PostSingleGridAdapter adapter) {
        adapter.setOnItemClickListener((view, viewObject, position) ->
                changeSelectedPostId(viewObject.getSelection() ? viewObject.id() : Constant.BAD_ID));
        adapter.setOnItemLongClickListener((view, viewObject, position) -> {
            if (isViewAttached()) getView().openPostViewScreen(viewObject.id());
        });
        adapter.setOnNewItemClickListener((view, viewObject, position) -> {
            if (isViewAttached()) getView().openPostCreateScreen();
        });
        adapter.setOnErrorClickListener((view) -> retryLoadMore());
    }

    @Override
    protected int getListTag() {
        return PostSingleGridFragment.RV_TAG;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PostCreateActivity.REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Timber.d("Post has been added resulting from screen with request code: %s", requestCode);
                    long postId = data.getLongExtra(OutConstants.OUT_EXTRA_POST_ID, Constant.BAD_ID);
                    getPostByIdUseCase.setPostId(postId);
                    getPostByIdUseCase.execute();
                }
                break;
        }
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void removeListItem(int position) {
        Timber.i("removeListItem: %s", position);
        int modelPosition = position;
        if (PostSingleGridAdapter.class.isInstance(listAdapter)) {
            // correction to the first element: (add new item)-element
            if (((PostSingleGridAdapter) listAdapter).withAddItem()) modelPosition -= 1;
        }
        long postId = posts.get(modelPosition).id();
        deletePostUseCase.setPostId(postId);
        deletePostUseCase.execute();  // silent delete without callback

        posts.remove(modelPosition);
        listAdapter.remove(position);

        if (posts.isEmpty()) {
            changeSelectedPostId(Constant.BAD_ID);  // drop selection
            if (isViewAttached()) getView().showEmptyList(getListTag());
        }
    }

    @Override
    public void retry() {
        Timber.i("retry");
        changeSelectedPostId(Constant.BAD_ID);  // drop selection
        deletePostUseCase.setPostId(Constant.BAD_ID);
        posts.clear();
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

    protected void retryLoadMore() {
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
    protected boolean changeSelectedPostId(long newId) {
        selectedPostId = newId;
        if (externalValueEmitter != null) {
            externalValueEmitter.emit(newId != Constant.BAD_ID);
            return true;
        }
        return false;
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    protected UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @Override
            public void onFinish(@Nullable Post post) {
                if (post == null) {
                    /**
                     * Post must not be null and GetPostById should be executed only as the result from
                     * PostCreateScreen returns and this result is OK and contains an id of newly created
                     * Post in repository. We then fetch this Post here and it must exists.
                     *
                     * If it doesn't exist or GetPostById is executed in any other way - this is actually
                     * a program error and {@link ProgramException} will be thrown.
                     */
                    Timber.e("Post wasn't found by id: %s", getPostByIdUseCase.getPostId());
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get Post by id");
                memento.currentSize += 1;
                PostSingleGridPresenter.this.posts.add(0, post);  // add post on top of the list
                PostSingleGridItemVO viewObject = postToSingleGridVoMapper.map(post);
                listAdapter.addInverse(viewObject);
                if (isViewAttached()) getView().showPosts(viewObject == null);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get Post by id");
                if (isViewAttached()) getView().showCreatePostFailure();
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected UseCase.OnPostExecuteCallback<List<Post>> createGetPostsCallback() {
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
                    memento.currentSize += posts.size();
                    PostSingleGridPresenter.this.posts = posts;
                    List<PostSingleGridItemVO> vos = postToSingleGridVoMapper.map(posts);
                    listAdapter.populate(vos, false);
                    if (isViewAttached()) getView().showPosts(vos == null || vos.isEmpty());
                }
            }

            @DebugLog @Override
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
