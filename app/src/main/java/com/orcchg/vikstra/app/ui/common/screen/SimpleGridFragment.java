package com.orcchg.vikstra.app.ui.common.screen;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.stub.SimpleBaseListFragment;
import com.orcchg.vikstra.app.ui.common.content.IScrollGrid;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public abstract class SimpleGridFragment extends SimpleBaseListFragment {

    protected @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;

    protected IScrollGrid iScrollGrid;

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
        View rootView = inflater.inflate(R.layout.rv_grid, container, false);
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
}
