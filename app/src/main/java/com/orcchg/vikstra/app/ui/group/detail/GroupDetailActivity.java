package com.orcchg.vikstra.app.ui.group.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.group.detail.injection.DaggerGroupDetailComponent;
import com.orcchg.vikstra.app.ui.group.detail.injection.GroupDetailComponent;
import com.orcchg.vikstra.app.ui.group.detail.injection.GroupDetailModule;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class GroupDetailActivity extends BaseActivity<GroupDetailContract.View, GroupDetailContract.Presenter>
        implements GroupDetailContract.View {
    private static final String BUNDLE_KEY_GROUP_ID = "bundle_key_group_id";
    private static final String EXTRA_GROUP_ID = "extra_group_id";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton fab;

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
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData(savedInstanceState);  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
//        ButterKnife.bind(this);
//        initView();
//        initToolbar();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(BUNDLE_KEY_GROUP_ID, groupId);
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    @DebugLog
    private void initData(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Timber.d("Restore state");
            groupId = savedInstanceState.getLong(BUNDLE_KEY_GROUP_ID, Constant.BAD_ID);
        } else {
            groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, Constant.BAD_ID);
        }
        Timber.d("Group id: %s", groupId);
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        // TODO: GroupDetailActivity: initView
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.group_detail_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onGroupLoaded(String url) {
        navigationComponent.navigator().openBrowser(this, url);
        finish();
    }
}
