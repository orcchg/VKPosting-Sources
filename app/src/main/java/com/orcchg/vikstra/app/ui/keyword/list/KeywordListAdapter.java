package com.orcchg.vikstra.app.ui.keyword.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.keyword.list.viewholder.KeywordViewHolder;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;

public class KeywordListAdapter extends BaseAdapter<KeywordViewHolder, KeywordListItemVO> {

    @Override
    protected KeywordViewHolder createModelViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_keywords, parent, false);
        return new KeywordViewHolder(view, onItemClickListener);
    }
}
