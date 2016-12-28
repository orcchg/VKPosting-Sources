package com.orcchg.vikstra.app.ui.post.single;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.common.screen.SimpleCollectionFragment;
import com.orcchg.vikstra.domain.util.Constant;

public class PostSingleGridFragment extends SimpleCollectionFragment implements PostSingleGridContract.SubView {
    public static final int RV_TAG = Constant.ListTag.POST_SINGLE_GRID_SCREEN;

    public static PostSingleGridFragment newInstance() {
        return new PostSingleGridFragment();
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        int span = getResources().getInteger(R.integer.post_single_grid_span);
        return new GridLayoutManager(getActivity(), span, GridLayoutManager.HORIZONTAL, false);
    }

    @Override
    protected boolean isGrid() {
        return true;
    }

    /* Contract */
    // ------------------------------------------
    @Override
    public void showPosts(boolean isEmpty) {
        showContent(isEmpty);
    }
}
