package com.orcchg.vikstra.app.ui.keyword.create;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.common.KeywordsFlowLayout;
import com.orcchg.vikstra.app.ui.keyword.create.injection.DaggerKeywordCreateComponent;
import com.orcchg.vikstra.app.ui.keyword.create.injection.KeywordCreateComponent;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import butterknife.BindView;
import butterknife.ButterKnife;

public class KeywordCreateActivity extends BaseActivity<KeywordCreateContract.View, KeywordCreateContract.Presenter>
    implements KeywordCreateContract.View {
    private static final String EXTRA_KEYWORDS = "extra_keywords";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.flow) KeywordsFlowLayout keywordsFlowLayout;

    private KeywordCreateComponent keywordCreateComponent;

    public static Intent getCallingIntent(@NonNull Context context) {
        return getCallingIntent(context, null);
    }

    public static Intent getCallingIntent(@NonNull Context context, @Nullable KeywordBundle keywords) {
        Intent intent = new Intent(context, KeywordCreateActivity.class);
        intent.putExtra(EXTRA_KEYWORDS, keywords);
        return intent;
    }

    @NonNull @Override
    protected KeywordCreateContract.Presenter createPresenter() {
        return keywordCreateComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        keywordCreateComponent = DaggerKeywordCreateComponent.builder()
                .applicationComponent(getApplicationComponent())
                .build();
        keywordCreateComponent.inject(this);
    }

    /* Lifecycle */
    // ------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keywords_create);
        ButterKnife.bind(this);
        initView();
        initToolbar();
    }

    /* View */
    // ------------------------------------------
    private void initView() {
        Intent intent = getIntent();
        KeywordBundle keywords = intent.getParcelableExtra(EXTRA_KEYWORDS);
        keywordsFlowLayout.setKeywords(keywords.keywords(), false);
    }

    private void initToolbar() {
        toolbar.setNavigationOnClickListener((view) -> finish());
    }
}
