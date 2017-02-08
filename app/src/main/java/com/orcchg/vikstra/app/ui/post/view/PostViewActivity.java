package com.orcchg.vikstra.app.ui.post.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.common.view.ThumbView;
import com.orcchg.vikstra.app.ui.post.view.injection.DaggerPostViewComponent;
import com.orcchg.vikstra.app.ui.post.view.injection.PostViewComponent;
import com.orcchg.vikstra.app.ui.post.view.injection.PostViewModule;
import com.orcchg.vikstra.app.ui.viewobject.PostViewVO;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class PostViewActivity extends BaseActivity<PostViewContract.View, PostViewContract.Presenter>
        implements PostViewContract.View {
    private static final String BUNDLE_KEY_POST_ID = "bundle_key_post_id";
    private static final String BUNDLE_KEY_EDITABLE = "bundle_key_editable";
    private static final String EXTRA_POST_ID = "extra_post_id";
    private static final String EXTRA_EDITABLE = "extra_editable";
    public static final int REQUEST_CODE = Constant.RequestCode.POST_VIEW_SCREEN;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.scroll_container) ViewGroup scrollContainer;
    @BindView(R.id.tv_post_description) TextView descriptionView;
    @BindView(R.id.primary_media_container) ViewGroup primaryMediaContainer;
    @BindView(R.id.iv_primary) ImageView primaryImage;
    @BindView(R.id.iv_secondary) ImageView secondaryImage;
    @BindView(R.id.media_container_root) ViewGroup mediaContainerRoot;
    @BindView(R.id.media_container) ViewGroup mediaContainer;
    @BindView(R.id.ll_link_container) ViewGroup linkContainer;
    @BindView(R.id.tv_link) TextView linkTextView;
    @BindView(R.id.wv_link) WebView linkWebView;
    @BindView(R.id.space) View space;
    @BindView(R.id.loading_view) View loadingView;
    @BindView(R.id.error_view) View errorView;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;
    @OnClick(R.id.btn_retry)
    void onRetryClick() {
        presenter.retry();
    }

    private int PRIMARY_MEDIA_DOUBLE_HEIGHT;

    private PostViewComponent postViewComponent;
    private long postId = Constant.BAD_ID;
    private boolean isEditable = false;

    public static Intent getCallingIntent(@NonNull Context context, long postId) {
        return getCallingIntent(context, postId, true);
    }

    public static Intent getCallingIntent(@NonNull Context context, long postId, boolean editable) {
        Intent intent = new Intent(context, PostViewActivity.class);
        intent.putExtra(EXTRA_POST_ID, postId);
        intent.putExtra(EXTRA_EDITABLE, editable);
        return intent;
    }

    @NonNull @Override
    protected PostViewContract.Presenter createPresenter() {
        return postViewComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        postViewComponent = DaggerPostViewComponent.builder()
                .applicationComponent(getApplicationComponent())
                .postViewModule(new PostViewModule(postId))
                .build();
        postViewComponent.inject(this);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData(savedInstanceState);  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);
        ButterKnife.bind(this);
        initResources();
        initToolbar();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(BUNDLE_KEY_POST_ID, postId);
        outState.putBoolean(BUNDLE_KEY_EDITABLE, isEditable);
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    @DebugLog
    private void initData(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            postId = savedInstanceState.getLong(BUNDLE_KEY_POST_ID, Constant.BAD_ID);
            isEditable = savedInstanceState.getBoolean(BUNDLE_KEY_EDITABLE, false);
        } else {
            postId = getIntent().getLongExtra(EXTRA_POST_ID, Constant.BAD_ID);
            isEditable = getIntent().getBooleanExtra(EXTRA_EDITABLE, false);
        }
        Timber.d("Post id: %s ; editable: %s", postId, isEditable);
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initToolbar() {
        toolbar.setTitle(R.string.post_view_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());
        if (isEditable) {
            toolbar.inflateMenu(R.menu.edit);
            toolbar.setOnMenuItemClickListener((item) -> {
                switch (item.getItemId()) {
                    case R.id.edit:
                        navigationComponent.navigator().openPostCreateScreen(this, postId);
                        return true;
                }
                return false;
            });
        }
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void setResultFromView(int resultCode) {
        setResult(resultCode);
    }

    // ------------------------------------------
    @Override
    public void showError() {
        dropshadowView.setVisibility(View.VISIBLE);
        scrollContainer.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoading() {
        dropshadowView.setVisibility(View.INVISIBLE);  // don't overlap with progress bar
        scrollContainer.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }

    @Override
    public void showPost(PostViewVO viewObject) {
        dropshadowView.setVisibility(View.VISIBLE);
        scrollContainer.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);

        mediaContainer.removeAllViews();
        descriptionView.setText(viewObject.description());
        int totalMedia = viewObject.media().size();
        if (totalMedia > 0) {
            primaryMediaContainer.setVisibility(View.VISIBLE);
            if (totalMedia >= 1) {
                primaryImage.setVisibility(View.VISIBLE);
                Glide.with(this).load(viewObject.media().get(0).url()).into(primaryImage);
                if (totalMedia >= 2) {
                    secondaryImage.setVisibility(View.VISIBLE);
                    Glide.with(this).load(viewObject.media().get(1).url()).into(secondaryImage);
                    if (totalMedia > 2) {
                        mediaContainerRoot.setVisibility(View.VISIBLE);
                        for (int i = 2; i < totalMedia; ++i) {
                            ThumbView thumbView = new ThumbView(this);
                            thumbView.setImage(viewObject.media().get(i).url());
                            mediaContainer.addView(thumbView);
                        }
                    }
                    return;  // ignore media container resizing for more than one media
                }
            }
        }

        // resize media container height to make single media squared
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) primaryMediaContainer.getLayoutParams();
        params.height = PRIMARY_MEDIA_DOUBLE_HEIGHT;
        primaryMediaContainer.setLayoutParams(params);
        space.setVisibility(View.GONE);  // remove space separator between primary and secondary medias

        showLink(viewObject.link());
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private void showLink(String link) {
        if (TextUtils.isEmpty(link)) {
            linkContainer.setVisibility(View.GONE);
        } else {
            linkContainer.setVisibility(View.VISIBLE);
            linkTextView.setText(link.toLowerCase());
            linkWebView.setWebViewClient(new WebViewClient());
            linkWebView.loadUrl(link);
        }
    }

    /* Resources */
    // --------------------------------------------------------------------------------------------
    private void initResources() {
        PRIMARY_MEDIA_DOUBLE_HEIGHT = getResources().getDimensionPixelSize(R.dimen.post_view_primary_media_height_double);
    }
}
