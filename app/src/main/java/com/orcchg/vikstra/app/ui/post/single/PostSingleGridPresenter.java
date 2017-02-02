package com.orcchg.vikstra.app.ui.post.single;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

    private Memento memento = new Memento();

    protected final @BaseSelectAdapter.SelectMode int selectMode;
    private ValueEmitter<Boolean> externalValueEmitter;

    private final PostToSingleGridVoMapper postToSingleGridVoMapper;

    // --------------------------------------------------------------------------------------------
    private static final class Memento {
        private static final String BUNDLE_KEY_POSTS = "bundle_key_posts";
        private static final String BUNDLE_KEY_SELECTED_POST_ID = "bundle_key_selected_post_id";
        private static final String BUNDLE_KEY_SELECTED_LIST_ITEM_POSITION = "bundle_key_selected_list_item_position";
        private static final String BUNDLE_KEY_WAS_LIST_ITEM_SELECTED = "bundle_key_was_list_item_selected";

        private List<Post> posts = new ArrayList<>();
        private long selectedPostId = Constant.BAD_ID;
        private int selectedListItemPosition = Constant.BAD_POSITION;
        private boolean wasListItemSelected = false;

        @DebugLog
        private void toBundle(Bundle outState) {
            if (ArrayList.class.isInstance(posts)) {
                outState.putParcelableArrayList(BUNDLE_KEY_POSTS, (ArrayList<Post>) posts);
            } else {
                ArrayList<Post> copyPosts = new ArrayList<>(posts);
                outState.putParcelableArrayList(BUNDLE_KEY_POSTS, copyPosts);
            }
            outState.putLong(BUNDLE_KEY_SELECTED_POST_ID, selectedPostId);
            outState.putInt(BUNDLE_KEY_SELECTED_LIST_ITEM_POSITION, selectedListItemPosition);
            outState.putBoolean(BUNDLE_KEY_WAS_LIST_ITEM_SELECTED, wasListItemSelected);
        }

        @DebugLog
        private static Memento fromBundle(Bundle savedInstanceState) {
            Memento memento = new Memento();
            memento.posts = savedInstanceState.getParcelableArrayList(BUNDLE_KEY_POSTS);
            if (memento.posts == null) memento.posts = new ArrayList<>();
            memento.selectedPostId = savedInstanceState.getLong(BUNDLE_KEY_SELECTED_POST_ID, Constant.BAD_ID);
            memento.selectedListItemPosition = savedInstanceState.getInt(BUNDLE_KEY_SELECTED_LIST_ITEM_POSITION, Constant.BAD_POSITION);
            memento.wasListItemSelected = savedInstanceState.getBoolean(BUNDLE_KEY_WAS_LIST_ITEM_SELECTED, false);
            return memento;
        }
    }

    // --------------------------------------------------------------------------------------------
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        memento.toBundle(outState);
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
        long postId = memento.posts.get(modelPosition).id();
        deletePostUseCase.setPostId(postId);
        deletePostUseCase.execute();  // silent delete without callback

        memento.posts.remove(modelPosition);
        listAdapter.remove(position);

        if (memento.posts.isEmpty()) {
            changeSelectedPostId(Constant.BAD_ID);  // drop selection
            if (isViewAttached()) getView().showEmptyList(getListTag());
        }
    }

    @Override
    public void retry() {
        Timber.i("retry");
        changeSelectedPostId(Constant.BAD_ID);  // drop selection
        deletePostUseCase.setPostId(Constant.BAD_ID);
        memento.posts.clear();
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
        return memento.selectedPostId;
    }

    @DebugLog
    protected boolean changeSelectedPostId(long newId) {
        memento.selectedPostId = newId;
        if (externalValueEmitter != null) {
            externalValueEmitter.emit(newId != Constant.BAD_ID);
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return memento.posts.isEmpty();
    }

    @Override
    protected void freshStart() {
        if (isViewAttached()) getView().showLoading(getListTag());
        getPostsUseCase.execute();
    }

    @Override
    protected void onRestoreState() {
        memento = Memento.fromBundle(savedInstanceState);
        boolean isEmpty = populateList(memento.posts);
        if (!isEmpty) {
            ((PostSingleGridAdapter) listAdapter).selectItemAtPosition(memento.selectedListItemPosition, memento.wasListItemSelected);
        }
        changeSelectedPostId(memento.selectedPostId);
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
                memento.posts.add(0, post);  // add post on top of the list
                listMemento.currentSize += 1;
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
                    memento.posts = posts;
                    listMemento.currentSize += posts.size();
                    populateList(posts);
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get list of Post-s");
                if (listMemento.currentSize <= 0) {
                    if (isViewAttached()) getView().showError(getListTag());
                } else {
                    listAdapter.onError(true);
                }
            }
        };
    }

    /* Utility */
    // --------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private boolean populateList(List<Post> posts) {
        List<PostSingleGridItemVO> vos = postToSingleGridVoMapper.map(posts);
        listAdapter.populate(vos, false);
        boolean isEmpty = vos == null || vos.isEmpty();
        if (isViewAttached()) getView().showPosts(isEmpty);
        return isEmpty;
    }

    // TODO: assign totalItems
}
