package com.orcchg.vikstra.app.ui.post.single.viewholder;

import android.view.View;

import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.widget.viewholder.BaseViewHolder;

import butterknife.ButterKnife;

public class NewPostSingleGridViewHolder extends BaseViewHolder {

    private BaseAdapter.OnItemClickListener<Object> listener;

    public NewPostSingleGridViewHolder(View view, BaseAdapter.OnItemClickListener<Object> listener) {
        super(view);
        ButterKnife.bind(this, view);
        this.listener = listener;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onItemClick(view, null, getAdapterPosition());
            }
        });
    }
}
