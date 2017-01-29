package com.orcchg.vikstra.app.ui.post.single.viewholder;

import android.view.View;

import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.adapter.viewholder.BaseViewHolder;

import butterknife.ButterKnife;

public class NewPostSingleGridViewHolder extends BaseViewHolder {

    private BaseAdapter.OnItemClickListener<Object> listener;

    public NewPostSingleGridViewHolder(View view, BaseAdapter.OnItemClickListener<Object> xlistener) {
        super(view);
        ButterKnife.bind(this, view);
        this.listener = xlistener;
        view.setOnClickListener((xview) -> {
            if (listener != null) listener.onItemClick(xview, null, getAdapterPosition());
        });
    }
}
