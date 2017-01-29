package com.orcchg.vikstra.app.ui.post.single;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.common.content.ISwipeToDismiss;
import com.orcchg.vikstra.app.ui.common.screen.SimpleCollectionFragment;
import com.orcchg.vikstra.app.ui.common.view.misc.GridItemDecorator;
import com.orcchg.vikstra.app.ui.post.single.viewholder.NewPostSingleGridViewHolder;
import com.orcchg.vikstra.domain.util.Constant;

public class PostSingleGridFragment extends SimpleCollectionFragment implements PostSingleGridContract.SubView {
    public static final int RV_TAG = Constant.ListTag.POST_SINGLE_GRID_SCREEN;

    private ISwipeToDismiss iSwipeToDismiss;

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
    // --------------------------------------------------------------------------------------------
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (ISwipeToDismiss.class.isInstance(context)) iSwipeToDismiss = (ISwipeToDismiss) context;
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        recyclerView.addItemDecoration(new GridItemDecorator(getActivity(), R.dimen.post_single_grid_item_spacing));
        createItemTouchHelper().attachToRecyclerView(recyclerView);
        emptyDataTextView.setText(R.string.post_single_grid_empty_data_text);
        emptyDataButton.setText(R.string.post_single_grid_empty_data_button_label);
        return view;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void showPosts(boolean isEmpty) {
        showContent(RV_TAG, isEmpty);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private ItemTouchHelper createItemTouchHelper() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP | ItemTouchHelper.DOWN) {
            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (NewPostSingleGridViewHolder.class.isInstance(viewHolder)) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if (iSwipeToDismiss != null) iSwipeToDismiss.onSwipeToDismiss(viewHolder.getAdapterPosition(), RV_TAG);
            }
        });
    }
}
