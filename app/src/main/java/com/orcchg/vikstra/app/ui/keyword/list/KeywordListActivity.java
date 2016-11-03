package com.orcchg.vikstra.app.ui.keyword.list;

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
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class KeywordListActivity extends SimpleBaseActivity implements ShadowHolder {

    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;

    public static Intent getCallingIntent(@NonNull Context context) {
        return new Intent(context, KeywordListActivity.class);
    }

    /* Lifecycle */
    // ------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keywords_list);
        ButterKnife.bind(this);
        initView();
        initToolbar();
    }

    /* View */
    // ------------------------------------------
    private void initView() {
        String tag = "list-fragment-tag";
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(tag) == null) {
            KeywordListFragment fragment = KeywordListFragment.newInstance();
            fm.beginTransaction().replace(R.id.container, fragment, tag).commit();
            fm.executePendingTransactions();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: mock data impl
                List<Keyword> music = new ArrayList<>();
                music.add(Keyword.create("Timbaland"));
                music.add(Keyword.create("Jodi Foster"));
                music.add(Keyword.create("Dima Bilan"));
                music.add(Keyword.create("Mark Aurelis"));
                music.add(Keyword.create("Sandro Sanders"));
                KeywordBundle keywords = KeywordBundle.builder().setTitle("Music").setKeywords(music).build();
                navigationComponent.navigator().openNewKeywordsBundleScreen(KeywordListActivity.this, keywords);
            }
        });
    }

    private void initToolbar() {
        toolbar.setNavigationOnClickListener((view) -> finish());
    }

    @Override
    public void showShadow(boolean show) {
        dropshadowView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }
}
