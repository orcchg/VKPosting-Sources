package com.orcchg.vikstra.app.ui.post.list;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridAdapter;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridContract;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridPresenter;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.domain.interactor.post.DeletePost;
import com.orcchg.vikstra.domain.interactor.post.GetPosts;

import javax.inject.Inject;

public class PostListPresenter extends PostSingleGridPresenter implements PostSingleGridContract.Presenter {

    @Inject
    public PostListPresenter(@BaseSelectAdapter.SelectMode int selectMode, GetPosts getPostsUseCase,
                             DeletePost deletePostUseCase, PostToSingleGridVoMapper postToSingleGridVoMapper) {
        super(selectMode, getPostsUseCase, deletePostUseCase, postToSingleGridVoMapper);
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
}
