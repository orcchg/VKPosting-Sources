package com.orcchg.vikstra.app.ui.post.single;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.post.single.viewholder.PostSingleGridViewHolder;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

public class PostSingleGridAdapter extends BaseAdapter<PostSingleGridViewHolder, PostSingleGridItemVO> {

    @Override
    protected PostSingleGridViewHolder createModelViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_post_single_grid_item, parent, false);
        return new PostSingleGridViewHolder(view, onItemClickListener);
    }
}
