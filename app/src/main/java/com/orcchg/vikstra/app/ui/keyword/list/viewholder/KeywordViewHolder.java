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

    private BaseAdapter.OnItemClickListener<KeywordListItemVO> listener;

    public KeywordViewHolder(View view, BaseAdapter.OnItemClickListener<KeywordListItemVO> listener) {
        super(view);
        ButterKnife.bind(this, view);
        this.listener = listener;
    }

    @Override
    public void bind(KeywordListItemVO viewObject) {
        flowView.setKeywords(viewObject.keywords());
        flowView.setTitle(viewObject.title());
        flowView.setSelection(viewObject.getSelection());

        itemView.setOnClickListener((view) -> {
            viewObject.setSelection(!viewObject.getSelection());
            flowView.setSelection(viewObject.getSelection());
            if (listener != null) listener.onItemClick(view, viewObject, getAdapterPosition());
        });
    }
}
