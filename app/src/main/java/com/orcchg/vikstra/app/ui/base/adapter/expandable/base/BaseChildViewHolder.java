package com.orcchg.vikstra.app.ui.base.adapter.expandable.base;

import android.view.View;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;

public abstract class BaseChildViewHolder<C extends BaseChildItem> extends ChildViewHolder<C> {

    public BaseChildViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(C model);
}
