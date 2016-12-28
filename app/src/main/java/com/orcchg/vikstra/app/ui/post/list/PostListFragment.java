package com.orcchg.vikstra.app.ui.post.list;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.common.screen.SimpleCollectionFragment;
import com.orcchg.vikstra.domain.util.Constant;

public class PostListFragment extends SimpleCollectionFragment implements PostListContract.SubView {
    public static final int RV_TAG = Constant.ListTag.POST_LIST_SCREEN;

    public static PostListFragment newInstance() {
        return new PostListFragment();
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        int span = getResources().getInteger(R.integer.post_list_span);
        return new GridLayoutManager(getActivity(), span, GridLayoutManager.VERTICAL, false);
    }

    @Override
    protected boolean isGrid() {
        return true;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void showPosts(boolean isEmpty) {
        swipeRefreshLayout.setRefreshing(false);
    }
}
