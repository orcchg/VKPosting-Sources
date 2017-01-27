package com.orcchg.vikstra.app.ui.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.common.view.misc.ImageTransform;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AvatarMenuItem extends FrameLayout {

    @BindView(R.id.iv_avatar) ImageView image;

    public AvatarMenuItem(Context context) {
        this(context, null);
    }

    public AvatarMenuItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarMenuItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    public void setImage(String url) {
        BitmapTransformation transformation = ImageTransform.create(getContext(), ImageTransform.CIRCLE_CROP);
        int size = getResources().getDimensionPixelSize(R.dimen.standard_icon_large_size);
        Glide.with(getContext()).load(url).asBitmap().override(size, size).transform(transformation)
                .error(R.drawable.avatar_placeholder).placeholder(R.drawable.avatar_placeholder).into(image);
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rootView = inflater.inflate(R.layout.avatar_menu_item, this, true);
        ButterKnife.bind(rootView);
    }
}
