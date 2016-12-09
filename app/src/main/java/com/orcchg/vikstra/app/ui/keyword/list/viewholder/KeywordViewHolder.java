package com.orcchg.vikstra.app.ui.keyword.list.viewholder;

import android.view.View;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.widget.viewholder.NormalViewHolder;
import com.orcchg.vikstra.app.ui.common.view.TitledFlowView;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;

import butterknife.BindView;
import butterknife.ButterKnife;

public class KeywordViewHolder extends NormalViewHolder<KeywordListItemVO> {

    @BindView(R.id.flow) TitledFlowView flowView;

    private final boolean isSelectable;
    private BaseAdapter.OnItemClickListener<KeywordListItemVO> editClickListener;

    public KeywordViewHolder(View view, boolean isSelectable) {
        super(view);
        ButterKnife.bind(this, view);
        this.isSelectable = isSelectable;
    }

    public void setOnEditClickListener(BaseAdapter.OnItemClickListener<KeywordListItemVO> editClickListener) {
        this.editClickListener = editClickListener;
    }

    @Override
    public void bind(KeywordListItemVO viewObject) {
        flowView.setKeywords(viewObject.keywords());
        flowView.setTitle(viewObject.title());
        flowView.setSelection(isSelectable ? viewObject.getSelection() : false);
        flowView.setEditable(editClickListener != null);
        flowView.setOnEditClickListener((view) -> {
            if (editClickListener != null) editClickListener.onItemClick(view, viewObject, getAdapterPosition());
        });

        itemView.setOnClickListener((view) -> {
            if (isSelectable) {
                viewObject.setSelection(!viewObject.getSelection());
                flowView.setSelection(viewObject.getSelection());
            }
            if (listener != null) listener.onItemClick(view, viewObject, getAdapterPosition());
        });

        itemView.setOnLongClickListener((view) -> {
            if (longListener != null) longListener.onItemLongClick(view, viewObject, getAdapterPosition());
            return false;
        });
    }
}
