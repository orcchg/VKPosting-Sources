package com.orcchg.vikstra.app.ui.base.widget.viewholder;

import android.view.View;

import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;

public abstract class NormalViewHolder<Model> extends BaseViewHolder {

    protected BaseAdapter.OnItemClickListener<Model> listener;
    protected BaseAdapter.OnItemLongClickListener<Model> longListener;

    public NormalViewHolder(View view) {
        super(view);
    }

    public abstract void bind(Model model);

    public void setOnItemClickListener(BaseAdapter.OnItemClickListener<Model> listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(BaseAdapter.OnItemLongClickListener<Model> listener) {
        this.longListener = listener;
    }
}
