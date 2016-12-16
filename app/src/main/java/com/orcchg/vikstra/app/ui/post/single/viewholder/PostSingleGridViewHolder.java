package com.orcchg.vikstra.app.ui.post.single.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.widget.viewholder.NormalViewHolder;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostSingleGridViewHolder extends NormalViewHolder<PostSingleGridItemVO> {

    @BindView(R.id.tv_title) TextView titleView;
    @BindView(R.id.tv_description) TextView descriptionView;
    @BindView(R.id.media_container_root) ViewGroup mediaContainerRoot;
    @BindView(R.id.iv_media) ImageView mediaView;
    @BindView(R.id.tv_media_count) TextView mediaCountView;
    @BindView(R.id.iv_selection) ImageView selectView;

    public PostSingleGridViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    @Override
    public void bind(PostSingleGridItemVO viewObject) {
        String title = viewObject.title();
        titleView.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        titleView.setText(title);

        String description = viewObject.description();
        descriptionView.setVisibility(TextUtils.isEmpty(description) ? View.GONE : View.VISIBLE);
        descriptionView.setText(description);

        String url = viewObject.hasMedia() ? viewObject.media().url() : "";
        mediaContainerRoot.setVisibility(viewObject.hasMedia() ? View.VISIBLE : View.GONE);
        mediaView.setVisibility(viewObject.hasMedia() ? View.VISIBLE : View.GONE);
        if (!TextUtils.isEmpty(url)) Glide.with(itemView.getContext()).load(url).into(mediaView);
        if (viewObject.mediaCount() > 1) {
            mediaCountView.setVisibility(View.VISIBLE);
            mediaCountView.setText(Integer.toString(viewObject.mediaCount()));
        } else {
            mediaCountView.setVisibility(View.GONE);
        }

        selectView.setVisibility(viewObject.getSelection() ? View.VISIBLE : View.INVISIBLE);

        itemView.setOnClickListener(createOnItemClickListener(viewObject));
        itemView.setOnLongClickListener(createOnItemLongClickListener(viewObject));
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
