package com.orcchg.vikstra.app.ui.group.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.stub.SimpleBaseActivity;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupListActivity extends SimpleBaseActivity implements ShadowHolder {
    private static final String FRAGMENT_TAG = "group_list_fragment_tag";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;

    public static Intent getCallingIntent(@NonNull Context context) {
        return new Intent(context, GroupListActivity.class);
    }

    /* Lifecycle */
    // ------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        ButterKnife.bind(this);
        initView();
        initToolbar();
    }

    /* View */
    // ------------------------------------------
    private void initView() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(FRAGMENT_TAG) == null) {
            GroupListFragment fragment = GroupListFragment.newInstance(1000);  // TODO: use proper id
            fm.beginTransaction().replace(R.id.container, fragment, FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.group_list_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());
    }

    @Override
    public void showShadow(boolean show) {
        dropshadowView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }
}
