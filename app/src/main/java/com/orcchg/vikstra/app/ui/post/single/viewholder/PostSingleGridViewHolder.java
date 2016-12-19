package com.orcchg.vikstra.app.ui.post.single.viewholder;

import android.view.View;
import android.widget.ImageView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.widget.viewholder.NormalViewHolder;
import com.orcchg.vikstra.app.ui.common.view.PostThumbnail;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostSingleGridViewHolder extends NormalViewHolder<PostSingleGridItemVO> {

    @BindView(R.id.post_thumbnail) PostThumbnail postThumbnail;
    @BindView(R.id.iv_selection) ImageView selectView;

    public PostSingleGridViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    @Override
    public void bind(PostSingleGridItemVO viewObject) {
        itemView.setOnClickListener(createOnItemClickListener(viewObject));
        itemView.setOnLongClickListener(createOnItemLongClickListener(viewObject));

        postThumbnail.setPost(viewObject);

        selectView.setVisibility(viewObject.getSelection() ? View.VISIBLE : View.INVISIBLE);
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private View.OnClickListener createOnItemClickListener(PostSingleGridItemVO viewObject) {
        return (view) -> {
            viewObject.setSelection(!viewObject.getSelection());
            selectView.setVisibility(viewObject.getSelection() ? View.VISIBLE : View.INVISIBLE);
            if (listener != null) listener.onItemClick(view, viewObject, getAdapterPosition());
        };
    }

    private View.OnLongClickListener createOnItemLongClickListener(PostSingleGridItemVO viewObject) {
        return (view) -> {
            if (longListener != null) longListener.onItemLongClick(view, viewObject, getAdapterPosition());
            return false;
        };
    }
}
