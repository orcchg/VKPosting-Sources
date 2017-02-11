package com.orcchg.vikstra.app.ui.common.view.behavior;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.orcchg.vikstra.R;

// {@see http://nemanjakovacevic.net/blog/english/2016/01/12/recyclerview-swipe-to-delete-no-3rd-party-lib-necessary/}
public abstract class DecoratedItemTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

    // we want to cache these and not allocate anything repeatedly in the onChildDraw method
    Drawable background;
    Drawable xMark;
    int xMarkMargin;
    boolean initiated;

    public DecoratedItemTouchHelperCallback(int dragDirs, int swipeDirs) {
        super(dragDirs, swipeDirs);
    }

    private void init(Resources resources) {
        background = new ColorDrawable(Color.RED);
        xMark = resources.getDrawable(R.drawable.ic_clear_white_24dp);
        xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        xMarkMargin = (int) resources.getDimension(R.dimen.standard_side_padding);
        initiated = true;
    }

//    @Override
//    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//        int position = viewHolder.getAdapterPosition();
//        TestAdapter testAdapter = (TestAdapter) recyclerView.getAdapter();
//        if (testAdapter.isUndoOn() && testAdapter.isPendingRemoval(position)) {
//            return 0;
//        }
//        return super.getSwipeDirs(recyclerView, viewHolder);
//    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

//    @Override
//    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//        int swipedPosition = viewHolder.getAdapterPosition();
//        TestAdapter adapter = (TestAdapter)mRecyclerView.getAdapter();
//        boolean undoOn = adapter.isUndoOn();
//        if (undoOn) {
//            adapter.pendingRemoval(swipedPosition);
//        } else {
//            adapter.remove(swipedPosition);
//        }
//    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;

        // not sure why, but this method get's called for viewholder that are already swiped away
        if (viewHolder.getAdapterPosition() == -1) {
            // not interested in those
            return;
        }

        if (!initiated) {
            init(recyclerView.getResources());
        }

        // draw red background
        background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        background.draw(c);

        // draw x mark
        int itemHeight = itemView.getBottom() - itemView.getTop();
        int intrinsicWidth = xMark.getIntrinsicWidth();
        int intrinsicHeight = xMark.getIntrinsicWidth();

        int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
        int xMarkRight = itemView.getRight() - xMarkMargin;
        int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
        int xMarkBottom = xMarkTop + intrinsicHeight;
        xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

        xMark.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
