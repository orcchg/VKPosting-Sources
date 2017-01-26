package com.orcchg.vikstra.app.ui.base.widget.expandable.viewholder;

import android.view.View;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.orcchg.vikstra.app.ui.base.widget.expandable.item.BaseChildItem;
import com.orcchg.vikstra.app.ui.base.widget.expandable.item.BaseParentItem;

public abstract class BaseParentViewHolder<P extends BaseParentItem<C>, C extends BaseChildItem>
        extends ParentViewHolder<P, C> {

    public BaseParentViewHolder(View itemView) {
        super(itemView);
    }
}
