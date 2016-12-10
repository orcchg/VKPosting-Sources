package com.orcchg.vikstra.app.ui.post.create;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.common.view.ThumbView;
import com.orcchg.vikstra.app.ui.post.create.injection.DaggerPostCreateComponent;
import com.orcchg.vikstra.app.ui.post.create.injection.PostCreateComponent;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostCreateActivity extends BaseActivity<PostCreateContract.View, PostCreateContract.Presenter>
        implements PostCreateContract.View {
    public static final int REQUEST_CODE = Constant.RequestCode.POST_CREATE_SCREEN;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.et_post_description) AutoCompleteTextView postDescriptionEditText;
    @BindView(R.id.media_container_root) ViewGroup mediaContainerRoot;
    @BindView(R.id.media_container) ViewGroup mediaContainer;
    @OnClick(R.id.ibtn_panel_location)
    public void onLocationButtonClick() {
        presenter.onLocationPressed();
    }
    @OnClick(R.id.ibtn_panel_media)
    public void onMediaButtonClick() {
        presenter.onMediaPressed();
    }
    @OnClick(R.id.ibtn_panel_attach)
    public void onAttachButtonClick() {
        presenter.onAttachPressed();
    }
    @OnClick(R.id.ibtn_panel_poll)
    public void onPollButtonClick() {
        presenter.onPollPressed();
    }

    private PostCreateComponent postCreateComponent;

    @NonNull @Override
    protected PostCreateContract.Presenter createPresenter() {
        return postCreateComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        postCreateComponent = DaggerPostCreateComponent.builder()
                .applicationComponent(getApplicationComponent())
                .build();
        postCreateComponent.inject(this);
    }

    public static Intent getCallingIntent(@NonNull Context context) {
        Intent intent = new Intent(context, PostCreateActivity.class);
        return intent;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_create);
        ButterKnife.bind(this);
        initView();
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        toolbar.setTitle(R.string.post_create_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());
        toolbar.inflateMenu(R.menu.save);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.save:
                    // TODO: save post
                    return true;
            }
            return false;
        });
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void addMedia() {
        // TODO: make container visible when necessary
        ThumbView mediaView = new ThumbView(this);
        mediaContainerRoot.setVisibility(View.VISIBLE);
        mediaContainer.addView(mediaView);
    }

    @Override
    public void clearInputText() {
        postDescriptionEditText.setText("");
    }

    @Override
    public String getInputText() {
        return postDescriptionEditText.getText().toString();
    }
}
