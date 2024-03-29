package com.orcchg.vikstra.app.ui.common.view.misc;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Decorates grid item.
 */
public class GridItemDecorator extends RecyclerView.ItemDecoration {
    private int itemOffset;

    public GridItemDecorator(int itemOffset) {
        this.itemOffset = itemOffset;
    }

    public GridItemDecorator(@NonNull Context context, @DimenRes int itemOffsetId) {
        this(context.getResources().getDimensionPixelSize(itemOffsetId));
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(itemOffset, itemOffset, itemOffset, itemOffset);
    }
}
