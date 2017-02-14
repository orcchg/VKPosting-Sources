package com.orcchg.vikstra.app.ui.report.history;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.stub.SimpleBaseActivity;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportHistoryActivity extends SimpleBaseActivity implements ShadowHolder {
    private static final String FRAGMENT_TAG = "report_history_fragment_tag";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;
    @BindView(R.id.fab) FloatingActionButton fab;

    public static Intent getCallingIntent(@NonNull Context context) {
        return new Intent(context, ReportHistoryActivity.class);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container_root);
        ButterKnife.bind(this);
        initView();
        initToolbar();
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        fab.hide();  // fab is not used on ReportHistoryScreen
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(FRAGMENT_TAG) == null) {
            ReportHistoryFragment fragment = ReportHistoryFragment.newInstance();
            fm.beginTransaction().replace(R.id.container, fragment, FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.report_history_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());  // close screen with current result
    }

    @Override
    public void showShadow(boolean show) {
        dropshadowView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }
}
