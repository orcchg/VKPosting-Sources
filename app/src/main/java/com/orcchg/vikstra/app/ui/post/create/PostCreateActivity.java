package com.orcchg.vikstra.app.ui.post.create;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.post.create.injection.DaggerPostCreateComponent;
import com.orcchg.vikstra.app.ui.post.create.injection.PostCreateComponent;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostCreateActivity extends BaseActivity<PostCreateContract.View, PostCreateContract.Presenter>
        implements PostCreateContract.View {

    @BindView(R.id.toolbar) Toolbar toolbar;

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
    // ------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_create);
        ButterKnife.bind(this);
        initView();
    }

    /* View */
    // ------------------------------------------
    private void initView() {
        toolbar.setTitle(R.string.post_create_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());
    }

    /* Contract */
    // ------------------------------------------
}
