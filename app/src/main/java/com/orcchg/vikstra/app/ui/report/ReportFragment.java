package com.orcchg.vikstra.app.ui.report;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.app.ui.common.screen.SimpleCollectionFragment;
import com.orcchg.vikstra.domain.util.Constant;

public class ReportFragment extends SimpleCollectionFragment implements ReportContract.SubView {
    public static final int RV_TAG = Constant.ListTag.REPORT_SCREEN;

    public static ReportFragment newInstance() {
        return new ReportFragment();
    }

    @Override
    protected boolean isGrid() {
        return false;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        createItemTouchHelper().attachToRecyclerView(recyclerView);
        return rootView;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void enableSwipeToRefresh(boolean isEnabled) {
        swipeRefreshLayout.setEnabled(isEnabled);
    }

    @Override
    public void showGroupReports(boolean isEmpty) {
        showContent(RV_TAG, isEmpty);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private ItemTouchHelper createItemTouchHelper() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        // TODO: delete wall post / remove from list
                        break;
                    case ItemTouchHelper.RIGHT:
                        // TODO: repeat wall post / restore wall post
                        break;
                }
            }
        });
    }
}
