package com.orcchg.vikstra.app.ui.legacy.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.legacy.list.viewholder.ArtistViewHolder;
import com.orcchg.vikstra.app.ui.viewobject.ArtistListItemVO;

class ListAdapter extends BaseAdapter<ArtistViewHolder, ArtistListItemVO> {

    private final ItemClickListener listener;

    ListAdapter(ItemClickListener listener) {
        super(BaseAdapter.SELECT_MODE_NONE);
        this.listener = listener;
    }

    @Override
    protected ArtistViewHolder createModelViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_musician, parent, false);
        return new ArtistViewHolder(view, listener);
    }
}
