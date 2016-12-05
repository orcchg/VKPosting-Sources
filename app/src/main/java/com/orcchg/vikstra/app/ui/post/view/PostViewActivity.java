package com.orcchg.vikstra.app.ui.post.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.post.view.injection.DaggerPostViewComponent;
import com.orcchg.vikstra.app.ui.post.view.injection.PostViewComponent;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostViewActivity extends BaseActivity<PostViewContract.View, PostViewContract.Presenter>
        implements PostViewContract.View {

    @BindView(R.id.toolbar) Toolbar toolbar;

    private PostViewComponent postViewComponent;

    public static Intent getCallingIntent(@NonNull Context context) {
        Intent intent = new Intent(context, PostViewActivity.class);
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
                .build();
        postViewComponent.inject(this);
    }

    /* Lifecycle */
    // ------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);
        ButterKnife.bind(this);
        initView();
    }

    /* View */
    // ------------------------------------------
    private void initView() {
        toolbar.setTitle(R.string.post_view_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());
    }
}
