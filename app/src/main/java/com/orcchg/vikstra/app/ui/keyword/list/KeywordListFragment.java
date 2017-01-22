package com.orcchg.vikstra.app.ui.keyword.list;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.app.ui.common.content.ISwipeToDismiss;
import com.orcchg.vikstra.app.ui.common.screen.SimpleCollectionFragment;
import com.orcchg.vikstra.domain.util.Constant;

public class KeywordListFragment extends SimpleCollectionFragment implements KeywordListContract.SubView {
    public static final int RV_TAG = Constant.ListTag.KEYWORD_LIST_SCREEN;

    private ISwipeToDismiss iSwipeToDismiss;

    public static KeywordListFragment newInstance() {
        return new KeywordListFragment();
    }

    @Override
    protected boolean isGrid() {
        return false;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (ISwipeToDismiss.class.isInstance(context)) {
            iSwipeToDismiss = (ISwipeToDismiss) context;
        }
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        createItemTouchHelper().attachToRecyclerView(recyclerView);
        return rootView;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void showKeywords(boolean isEmpty) {
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
                if (iSwipeToDismiss != null) iSwipeToDismiss.onSwipeToDismiss(viewHolder.getAdapterPosition());
            }
        });
    }
}
