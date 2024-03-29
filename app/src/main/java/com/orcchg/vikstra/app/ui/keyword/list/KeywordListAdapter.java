package com.orcchg.vikstra.app.ui.keyword.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.keyword.list.viewholder.KeywordViewHolder;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;

import hugo.weaving.DebugLog;

public class KeywordListAdapter extends BaseSelectAdapter<KeywordViewHolder, KeywordListItemVO> {

    private BaseAdapter.OnItemClickListener<KeywordListItemVO> editClickListener;

    public KeywordListAdapter(@SelectMode int selectMode) {
        super(selectMode);
    }

    public void setOnEditClickListener(BaseAdapter.OnItemClickListener<KeywordListItemVO> editClickListener) {
        this.editClickListener = editClickListener;
    }

    @Override
    protected KeywordViewHolder createModelViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_keywords, parent, false);
        KeywordViewHolder viewHolder = new KeywordViewHolder(view, selectMode);
        viewHolder.setOnItemClickListener(wrappedItemClickListener);
        viewHolder.setOnItemLongClickListener(onItemLongClickListener);
        viewHolder.setOnEditClickListener(editClickListener);
        return viewHolder;
    }

    @DebugLog
    public void selectItemAtPosition(int modelPosition, boolean isSelected) {
        KeywordListItemVO vo = models.get(modelPosition);
        vo.setSelection(isSelected);
        notifyItemChanged(modelPosition);
    }
}
