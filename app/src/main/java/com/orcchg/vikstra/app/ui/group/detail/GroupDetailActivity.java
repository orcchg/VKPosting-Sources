package com.orcchg.vikstra.app.ui.group.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.group.detail.injection.DaggerGroupDetailComponent;
import com.orcchg.vikstra.app.ui.group.detail.injection.GroupDetailComponent;
import com.orcchg.vikstra.app.ui.group.detail.injection.GroupDetailModule;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupDetailActivity extends BaseActivity<GroupDetailContract.View, GroupDetailContract.Presenter>
        implements GroupDetailContract.View {
    private static final String EXTRA_GROUP_ID = "extra_group_id";

    @BindView(R.id.toolbar) Toolbar toolbar;

    private GroupDetailComponent groupDetailComponent;
    private long groupId = Constant.BAD_ID;

    public static Intent getCallingIntent(@NonNull Context context, long groupId) {
        Intent intent = new Intent(context, GroupDetailActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        return intent;
    }

    @NonNull @Override
    protected GroupDetailContract.Presenter createPresenter() {
        return groupDetailComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        groupDetailComponent = DaggerGroupDetailComponent.builder()
                .applicationComponent(getApplicationComponent())
                .groupDetailModule(new GroupDetailModule(groupId))
                .build();
        groupDetailComponent.inject(this);
    }

    /* Lifecycle */
    // ------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData();  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        ButterKnife.bind(this);
        initView();
        initToolbar();
    }

    /* Data */
    // ------------------------------------------
    private void initData() {
        groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, Constant.BAD_ID);
    }

    /* View */
    // ------------------------------------------
    private void initView() {
        //
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.group_detail_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());
    }
}
