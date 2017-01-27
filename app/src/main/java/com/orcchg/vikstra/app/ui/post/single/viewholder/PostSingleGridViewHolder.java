package com.orcchg.vikstra.app.ui.post.single.viewholder;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.adapter.viewholder.NormalViewHolder;
import com.orcchg.vikstra.app.ui.common.view.PostThumbnail;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostSingleGridViewHolder extends NormalViewHolder<PostSingleGridItemVO> {

    @BindView(R.id.rl_container) ViewGroup container;
    @BindView(R.id.post_thumbnail) PostThumbnail postThumbnail;
    @BindView(R.id.iv_selection) ImageView selectView;

    private static @ColorInt int sNormalColor = -1;
    private static @ColorInt int sSelectedColor = -1;

    public PostSingleGridViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        Context context = itemView.getContext();
        if (sNormalColor == -1) sNormalColor = ContextCompat.getColor(context, R.color.grey_bg);
        if (sSelectedColor == -1) sSelectedColor = ContextCompat.getColor(context, R.color.colorAccent);
    }

    @Override
    public void bind(PostSingleGridItemVO viewObject) {
        itemView.setOnClickListener(createOnItemClickListener(viewObject));
        itemView.setOnLongClickListener(createOnItemLongClickListener(viewObject));

        postThumbnail.setPost(viewObject);

        if (viewObject.getSelection()) {
            container.setBackgroundColor(sSelectedColor);
            postThumbnail.setTitleTextColor(R.color.white);
            postThumbnail.setDescriptionTextColor(R.color.white);
            postThumbnail.showMediaOverlay(false);
            selectView.setVisibility(View.VISIBLE);
        } else {
            container.setBackgroundColor(sNormalColor);
            postThumbnail.setTitleTextColor(0);
            postThumbnail.setDescriptionTextColor(0);
            postThumbnail.showMediaOverlay(true);
            selectView.setVisibility(View.INVISIBLE);
        }
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
