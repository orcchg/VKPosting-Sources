package com.orcchg.vikstra.app.ui.base.adapter.expandable.base.stub;

import android.view.View;

import com.orcchg.vikstra.app.ui.base.adapter.expandable.base.BaseChildViewHolder;

public class SimpleBaseChildViewHolder extends BaseChildViewHolder<ChildItemStub> {

    public SimpleBaseChildViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(ChildItemStub model) {
        // override in subclasses
    }
}
