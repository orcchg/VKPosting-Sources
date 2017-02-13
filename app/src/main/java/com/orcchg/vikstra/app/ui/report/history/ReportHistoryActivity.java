package com.orcchg.vikstra.app.ui.report.history;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.stub.SimpleBaseActivity;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportHistoryActivity extends SimpleBaseActivity implements ShadowHolder {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;

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
        //
    }

    private void initToolbar() {
        //
    }

    @Override
    public void showShadow(boolean show) {
        dropshadowView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }
}
