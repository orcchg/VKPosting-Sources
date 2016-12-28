package com.orcchg.vikstra.app.ui.post.single;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.widget.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.util.ValueEmitter;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.post.GetPosts;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

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
        return adapter;
    }

    @Override
    protected int getListTag() {
        return PostSingleGridFragment.RV_TAG;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onScroll(int itemsLeftToEnd) {
        // TODO: load more
    }

    @DebugLog @Override
    public void retry() {
        changeSelectedPostId(Constant.BAD_ID);  // drop selection
        listAdapter.clear();
        freshStart();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    public long getSelectedPostId() {
        return selectedPostId;
    }

    @Override
    protected void freshStart() {
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
