package com.orcchg.vikstra.app.ui.post.list;

import android.app.Activity;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridAdapter;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridPresenter;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.domain.interactor.post.DeletePost;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.post.GetPosts;
import com.orcchg.vikstra.domain.util.Constant;

import javax.inject.Inject;

public class PostListPresenter extends PostSingleGridPresenter implements PostListContract.Presenter {
    private static final int PrID = Constant.PresenterId.POST_LIST_PRESENTER;

    @Inject
    public PostListPresenter(@BaseSelectAdapter.SelectMode int selectMode, GetPostById getPostByIdUseCase,
                             GetPosts getPostsUseCase, DeletePost deletePostUseCase,
                             PostToSingleGridVoMapper postToSingleGridVoMapper) {
        super(selectMode, getPostByIdUseCase, getPostsUseCase, deletePostUseCase, postToSingleGridVoMapper);
    }

    @Override
    protected BaseAdapter createListAdapter() {
        // slightly change item layout for this adapter
        PostSingleGridAdapter adapter = new PostSingleGridAdapter(selectMode, false) {
            @Override
            public int getItemLayout() {
                return R.layout.rv_post_list_item;
            }
        };
        prepareAdapter(adapter);
        return adapter;
    }

    @Override
    protected int getListTag() {
        return PostListFragment.RV_TAG;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onSelectPressed() {
        long postId = getSelectedPostId();
        if (postId != Constant.BAD_ID) {
            // TODO: arch limitation workaround - cast
            if (isViewAttached()) ((PostListContract.View) getView()).closeView(Activity.RESULT_OK, postId);
        } else {
            if (isViewAttached()) ((PostListContract.View) getView()).onPostNotSelected();
        }
    }
}
