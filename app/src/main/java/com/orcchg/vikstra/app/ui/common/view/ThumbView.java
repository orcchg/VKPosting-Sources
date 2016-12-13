package com.orcchg.vikstra.app.ui.common.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.orcchg.vikstra.R;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ThumbView extends FrameLayout {
    public static final int SIZE_NORMAL = 0;
    public static final int SIZE_SMALL = 1;
    @IntDef({SIZE_NORMAL, SIZE_SMALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Size {}

    protected @Size int sizeType;

    protected View rootView;
    @BindView(R.id.iv_image) ImageView image;

    public ThumbView(Context context) {
        this(context, null);
    }
    public ThumbView(Context context, @Size int sizeType) {
        this(context, sizeType, null);
    }

    public ThumbView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ThumbView(Context context, @Size int sizeType, AttributeSet attrs) {
        this(context, sizeType, attrs, 0);
    }

    public ThumbView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, SIZE_NORMAL, attrs, defStyleAttr);
    }
    public ThumbView(Context context, @Size int sizeType, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.sizeType = sizeType;
        initView();
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    public void setImage(Bitmap bmp) {
        image.setImageBitmap(bmp);
    }

    public void setImageLocal(String filePath) {
        Glide.with(getContext()).load(Uri.fromFile(new File(filePath)))
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Timber.wtf(e, " WTF ");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Timber.i("OKAY");
                        return false;
                    }
                })
                .into(image);
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        @LayoutRes int layoutRes = -1;
        switch (sizeType) {
            case SIZE_NORMAL:
                layoutRes = R.layout.thumb_view;
                break;
            case SIZE_SMALL:
                layoutRes = R.layout.thumb_small_view;
                break;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        rootView = inflater.inflate(layoutRes, this, true);
        ButterKnife.bind(rootView);
    }
}
