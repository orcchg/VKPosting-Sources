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
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupListActivity extends SimpleBaseActivity implements ShadowHolder {
    private static final String FRAGMENT_TAG = "group_list_fragment_tag";
    private static final String EXTRA_KEYWORD_BUNDLE_ID = "extra_keyword_bundle_id";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;

    private long keywordBundleId = Constant.BAD_ID;

    public static Intent getCallingIntent(@NonNull Context context, long keywordBunldeId) {
        Intent intent = new Intent(context, GroupListActivity.class);
        intent.putExtra(EXTRA_KEYWORD_BUNDLE_ID, keywordBunldeId);
        return intent;
    }

    /* Lifecycle */
    // ------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData();  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        ButterKnife.bind(this);
        initView();
        initToolbar();
    }

    /* Data */
    // ------------------------------------------
    private void initData() {
        keywordBundleId = getIntent().getLongExtra(EXTRA_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
    }

    /* View */
    // ------------------------------------------
    private void initView() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(FRAGMENT_TAG) == null) {
            GroupListFragment fragment = GroupListFragment.newInstance(keywordBundleId);
            fm.beginTransaction().replace(R.id.container, fragment, FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.group_list_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());
        toolbar.inflateMenu(R.menu.save);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.save:
                    // TODO: save
                    return true;
            }
            return false;
        });
    }

    @Override
    public void showShadow(boolean show) {
        dropshadowView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }
}
