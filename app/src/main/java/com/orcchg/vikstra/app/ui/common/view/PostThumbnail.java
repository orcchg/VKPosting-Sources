package com.orcchg.vikstra.app.ui.common.view;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.util.UiUtility;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostThumbnail extends FrameLayout {

    protected View rootView;
    @BindView(R.id.tv_title) TextView titleView;
    @BindView(R.id.tv_description) TextView descriptionView;
    @BindView(R.id.tv_empty_data) TextView emptyDataView;
    @BindView(R.id.media_container_root) ViewGroup mediaContainerRoot;
    @BindView(R.id.iv_media) ImageView mediaView;
    @BindView(R.id.tv_media_count) TextView mediaCountView;
    @BindView(R.id.media_overlay) View mediaOverlay;
    @BindView(R.id.ll_error_container) ViewGroup errorContainer;
    @BindView(R.id.btn_retry) ImageButton errorRetryButton;
    @OnClick(R.id.btn_retry)
    void onRetryClick() {
        if (retryClickListener != null) retryClickListener.onClick(errorRetryButton);
    }

    private OnClickListener retryClickListener;

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
        if (UiUtility.isVisible(emptyDataView)) emptyDataView.setVisibility(TextUtils.isEmpty(title) ? View.VISIBLE : View.GONE);
        titleView.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        titleView.setText(title);
    }

    public void setTitle(@StringRes int resId) {
        if (UiUtility.isVisible(emptyDataView)) emptyDataView.setVisibility(resId <= 0 ? View.VISIBLE : View.GONE);
        titleView.setVisibility(resId <= 0 ? View.GONE : View.VISIBLE);
        titleView.setText(resId);
    }

    public void setTitleTextColor(@ColorRes int resId) {
        titleView.setTextColor(getTextColorByRes(resId));
    }

    public void setDescription(String description) {
        if (UiUtility.isVisible(emptyDataView)) emptyDataView.setVisibility(TextUtils.isEmpty(description) ? View.VISIBLE : View.GONE);
        descriptionView.setVisibility(TextUtils.isEmpty(description) ? View.GONE : View.VISIBLE);
        descriptionView.setText(description);
    }

    public void setDescription(@StringRes int resId) {
        if (UiUtility.isVisible(emptyDataView)) emptyDataView.setVisibility(resId <= 0 ? View.VISIBLE : View.GONE);
        descriptionView.setVisibility(resId <= 0 ? View.GONE : View.VISIBLE);
        descriptionView.setText(resId);
    }

    public void setDescriptionTextColor(@ColorRes int resId) {
        descriptionView.setTextColor(getTextColorByRes(resId));
    }

    public void setMedia(String url) {
        boolean hasMedia = !TextUtils.isEmpty(url);
        if (UiUtility.isVisible(emptyDataView)) emptyDataView.setVisibility(hasMedia ? View.GONE : View.VISIBLE);
        mediaContainerRoot.setVisibility(hasMedia ? View.VISIBLE : View.GONE);
        mediaView.setVisibility(hasMedia ? View.VISIBLE : View.GONE);
        if (hasMedia) Glide.with(getContext()).load(url).into(mediaView);
    }

    public void setMediaCount(int count) {
        if (count > 1) {
            if (UiUtility.isVisible(emptyDataView)) emptyDataView.setVisibility(View.GONE);
            mediaCountView.setVisibility(View.VISIBLE);
            mediaCountView.setText(Integer.toString(count));
        } else {
            mediaCountView.setVisibility(View.GONE);
        }
    }

    public void showMediaOverlay(boolean isVisible) {
        mediaOverlay.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void setPost(@Nullable PostSingleGridItemVO viewObject) {
        if (viewObject != null) {
            if (UiUtility.isVisible(emptyDataView)) emptyDataView.setVisibility(View.GONE);
            setTitle(viewObject.title());
            setDescription(viewObject.description());
            String url = viewObject.hasMedia() ? viewObject.media().url() : "";
            setMedia(url);
            setMediaCount(viewObject.mediaCount());
        } else {
            emptyDataView.setVisibility(View.VISIBLE);
        }
    }

    // ------------------------------------------
    public void setErrorRetryButtonClickListener(OnClickListener listener) {
        retryClickListener = listener;
    }

    public void showError(boolean isVisible) {
        // TODO: make proper showError, handle visibility of other elements
//        errorContainer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
//
//        if (UiUtility.isVisible(emptyDataView)) emptyDataView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
//        if (UiUtility.isVisible(titleView)) titleView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
//        if (UiUtility.isVisible(descriptionView)) descriptionView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
//        if (UiUtility.isVisible(mediaContainerRoot)) mediaContainerRoot.setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        rootView = inflater.inflate(R.layout.post_thumb_content, this, true);
        ButterKnife.bind(rootView);
    }

    @ColorInt
    private int getTextColorByRes(@ColorRes int resId) {
        @ColorInt int color = getResources().getColor(R.color.textSecondary);
        if (resId > 0) color = getResources().getColor(resId);
        return color;
    }
}
