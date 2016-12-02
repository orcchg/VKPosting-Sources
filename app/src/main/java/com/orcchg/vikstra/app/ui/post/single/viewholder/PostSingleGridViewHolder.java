package com.orcchg.vikstra.app.ui.post.single.viewholder;

import android.view.View;

import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.widget.viewholder.NormalViewHolder;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

import butterknife.ButterKnife;

public class PostSingleGridViewHolder extends NormalViewHolder<PostSingleGridItemVO> {

    private BaseAdapter.OnItemClickListener<PostSingleGridItemVO> listener;

    public PostSingleGridViewHolder(View view, BaseAdapter.OnItemClickListener<PostSingleGridItemVO> listener) {
        super(view);
        ButterKnife.bind(this, view);
        this.listener = listener;
    }

    @Override
    public void bind(PostSingleGridItemVO postSingleGridItemVO) {
        // TODO: bind
    }
}
