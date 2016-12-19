package com.orcchg.vikstra.app.ui.common.view;

import android.content.Context;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostThumbnail extends FrameLayout {

    protected View rootView;
    @BindView(R.id.tv_title) TextView titleView;
    @BindView(R.id.tv_description) TextView descriptionView;
    @BindView(R.id.media_container_root) ViewGroup mediaContainerRoot;
    @BindView(R.id.iv_media) ImageView mediaView;
    @BindView(R.id.tv_media_count) TextView mediaCountView;

    public PostThumbnail(Context context) {
        this(context, null);
    }

    public PostThumbnail(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PostThumbnail(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    public void setTitle(String title) {
        titleView.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        titleView.setText(title);
    }

    public void setTitle(@StringRes int resId) {
        titleView.setVisibility(resId > 0 ? View.GONE : View.VISIBLE);
        titleView.setText(resId);
    }

    public void setDescription(String description) {
        descriptionView.setVisibility(TextUtils.isEmpty(description) ? View.GONE : View.VISIBLE);
        descriptionView.setText(description);
    }

    public void setDescription(@StringRes int resId) {
        descriptionView.setVisibility(resId > 0 ? View.GONE : View.VISIBLE);
        descriptionView.setText(resId);
    }

    public void setMedia(String url) {
        boolean hasMedia = !TextUtils.isEmpty(url);
        mediaContainerRoot.setVisibility(hasMedia ? View.VISIBLE : View.GONE);
        mediaView.setVisibility(hasMedia ? View.VISIBLE : View.GONE);
        if (hasMedia) Glide.with(getContext()).load(url).into(mediaView);
    }

    public void setMediaCount(int count) {
        if (count > 1) {
            mediaCountView.setVisibility(View.VISIBLE);
            mediaCountView.setText(Integer.toString(count));
        } else {
            mediaCountView.setVisibility(View.GONE);
        }
    }

    public void setPost(PostSingleGridItemVO viewObject) {
        setTitle(viewObject.title());
        setDescription(viewObject.description());
        String url = viewObject.hasMedia() ? viewObject.media().url() : "";
        setMedia(url);
        setMediaCount(viewObject.mediaCount());
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        rootView = inflater.inflate(R.layout.post_thumb_content, this, true);
        ButterKnife.bind(rootView);
    }
}
