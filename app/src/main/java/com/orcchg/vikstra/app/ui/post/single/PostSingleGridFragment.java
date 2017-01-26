package com.orcchg.vikstra.app.ui.post.single;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    @Override
    protected boolean autoFit() {
        return true;
    }

    /* Lifecycle */
    // ------------------------------------------
    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        emptyDataTextView.setText(R.string.post_single_grid_empty_data_text);
        emptyDataButton.setText(R.string.post_single_grid_empty_data_button_label);
        return view;
    }

    /* Contract */
    // ------------------------------------------
    @Override
    public void showPosts(boolean isEmpty) {
        showContent(RV_TAG, isEmpty);
    }
}
