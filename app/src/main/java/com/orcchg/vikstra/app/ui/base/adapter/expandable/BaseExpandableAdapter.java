package com.orcchg.vikstra.app.ui.base.adapter.expandable;

import android.support.annotation.NonNull;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.orcchg.vikstra.app.ui.base.adapter.expandable.item.BaseChildItem;
import com.orcchg.vikstra.app.ui.base.adapter.expandable.item.BaseParentItem;
import com.orcchg.vikstra.app.ui.base.adapter.expandable.viewholder.BaseChildViewHolder;
import com.orcchg.vikstra.app.ui.base.adapter.expandable.viewholder.BaseParentViewHolder;

import java.util.List;

public abstract class BaseExpandableAdapter<P extends BaseParentItem<C>, C extends BaseChildItem,
                                            PVH extends BaseParentViewHolder<P, C>,
                                            CVH extends BaseChildViewHolder<C>>
        extends ExpandableRecyclerAdapter<P, C, PVH, CVH> {

    public BaseExpandableAdapter(@NonNull List<P> parentItems) {
        super(parentItems);
    }
}
