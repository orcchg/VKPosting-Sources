package com.orcchg.vikstra.app.ui.status;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.github.jorgecastillo.FillableLoader;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.stub.SimpleBaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatusActivity extends SimpleBaseActivity {

    @BindView(R.id.fillable_loader) FillableLoader fillableLoader;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, StatusActivity.class);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        ButterKnife.bind(this);
        initView();
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        fillableLoader.setSvgPath(getResources().getString(R.string.status_default_svg_path));
        fillableLoader.start();
    }
}
