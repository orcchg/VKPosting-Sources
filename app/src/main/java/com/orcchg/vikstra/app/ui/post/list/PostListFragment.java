package com.orcchg.vikstra.app.ui.post.list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.common.screen.SimpleCollectionFragment;
import com.orcchg.vikstra.app.ui.common.view.misc.GridItemDecorator;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridContract;
import com.orcchg.vikstra.domain.util.Constant;

public class PostListFragment extends SimpleCollectionFragment implements PostSingleGridContract.SubView {
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

    /* Lifecycle */
    // ------------------------------------------
    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        recyclerView.addItemDecoration(new GridItemDecorator(getActivity(), R.dimen.post_list_item_spacing));
        emptyDataTextView.setText(R.string.post_list_empty_data_text);
        emptyDataButton.setText(R.string.post_list_empty_data_button_label);
        return view;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void showPosts(boolean isEmpty) {
        showContent(RV_TAG, isEmpty);
    }
}
