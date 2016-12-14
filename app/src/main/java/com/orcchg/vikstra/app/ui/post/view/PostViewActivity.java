package com.orcchg.vikstra.app.ui.post.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class PostViewActivity extends BaseActivity<PostViewContract.View, PostViewContract.Presenter>
        implements PostViewContract.View {
    private static final String EXTRA_POST_ID = "extra_post_id";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tv_post_description) TextView descriptionView;
    @BindView(R.id.primary_media_container) ViewGroup primaryMediaContainer;
    @BindView(R.id.iv_primary) ImageView primaryImage;
    @BindView(R.id.iv_secondary) ImageView secondaryImage;
    @BindView(R.id.media_container_root) ViewGroup mediaContainerRoot;
    @BindView(R.id.media_container) ViewGroup mediaContainer;

    private PostViewComponent postViewComponent;
    private long postId = Constant.BAD_ID;

    public static Intent getCallingIntent(@NonNull Context context, long postId) {
        Intent intent = new Intent(context, PostViewActivity.class);
        intent.putExtra(EXTRA_POST_ID, postId);
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
        initData();  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);
        ButterKnife.bind(this);
        initView();
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    private void initData() {
        postId = getIntent().getLongExtra(EXTRA_POST_ID, Constant.BAD_ID);
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        toolbar.setTitle(R.string.post_view_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void showPost(PostViewVO viewObject) {
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
                }
            }
        }
    }
}
