package com.orcchg.vikstra.app.ui.post.single;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.stub.SimpleBaseListFragment;
import com.orcchg.vikstra.app.ui.common.content.IScrollGrid;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PostSingleGridFragment extends SimpleBaseListFragment implements PostSingleGridContract.SubView {
    public static final int RV_TAG = Constant.ListTag.POST_SINGLE_GRID_SCREEN;

    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;

    private IScrollGrid iScrollGrid;

    public static PostSingleGridFragment newInstance() {
        return new PostSingleGridFragment();
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        int span = getResources().getInteger(R.integer.post_single_grid_span);
        return new GridLayoutManager(getActivity(), span, GridLayoutManager.HORIZONTAL, false);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (IScrollGrid.class.isInstance(activity)) {
            iScrollGrid = (IScrollGrid) activity;
        } else {
            String message = "Hosting Activity must implement IScrollList interface";
            Timber.e(message);
            throw new RuntimeException(message);
        }
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_post_single_grid, container, false);
        ButterKnife.bind(this, rootView);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_grid);

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(() -> iScrollGrid.retryGrid());

        return rootView;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onScroll(int itemsLeftToEnd) {
        // TODO: impl
    }

    @Override
    public void showPosts(boolean isEmpty) {
        swipeRefreshLayout.setRefreshing(false);
    }
}
