package com.orcchg.vikstra.app.ui.legacy.list.viewholder;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.widget.viewholder.NormalViewHolder;
import com.orcchg.vikstra.app.ui.legacy.list.ItemClickListener;
import com.orcchg.vikstra.app.ui.viewobject.ArtistListItemVO;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArtistViewHolder extends NormalViewHolder<ArtistListItemVO> {

    private final ItemClickListener listener;

    @Nullable @BindView(R.id.fl_grid_item) View gridItemView;  // only on large screens
    @BindView(R.id.pb_loading) ProgressBar progressBar;
    @BindView(R.id.iv_cover) ImageView iconView;
    @BindView(R.id.tv_musician_title) TextView titleView;

    public ArtistViewHolder(View view, ItemClickListener listener) {
        super(view);
        this.listener = listener;
        ButterKnife.bind(this, view);
    }

    @Override
    public void bind(ArtistListItemVO viewObject) {
        final Context context = itemView.getContext();

        itemView.setOnClickListener((view) -> listener.onItemClick(iconView, viewObject.getId()));

        if (gridItemView != null) {
            gridItemView.setOnClickListener((view) -> listener.onItemClick(iconView, viewObject.getId()));
        }

        titleView.setText(viewObject.getName());

        Glide.with(context)
            .load(viewObject.getCoverSmall())
            .listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            })
            .into(iconView);
    }
}
