package com.orcchg.vikstra.app.ui.keyword.create;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.widget.AutoCompleteTextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.app.ui.common.view.KeywordsFlowLayout;
import com.orcchg.vikstra.app.ui.keyword.create.injection.DaggerKeywordCreateComponent;
import com.orcchg.vikstra.app.ui.keyword.create.injection.KeywordCreateComponent;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import butterknife.BindView;
import butterknife.ButterKnife;

public class KeywordCreateActivity extends BaseActivity<KeywordCreateContract.View, KeywordCreateContract.Presenter>
        implements KeywordCreateContract.View {
    private static final String EXTRA_KEYWORDS = "extra_keywords";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.flow) KeywordsFlowLayout keywordsFlowLayout;
    @BindView(R.id.et_keyword_input) AutoCompleteTextView inputEditText;
    @BindView(R.id.fab) FloatingActionButton fab;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        inputEditText.requestFocus();
    }

    /* View */
    // ------------------------------------------
    private void initView() {
        Intent intent = getIntent();
        KeywordBundle keywords = intent.getParcelableExtra(EXTRA_KEYWORDS);
        String dialogTitle = getResources().getString(R.string.dialog_input_keywords_bundle_title);
        String dialogInputHint = getResources().getString(R.string.dialog_input_keywords_bundle_hint);
        keywordsFlowLayout.setKeywords(keywords.keywords());
        toolbar.setTitle(keywords.title());
        toolbar.setNavigationOnClickListener((view) -> finish());
        toolbar.inflateMenu(R.menu.edit_save);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.edit:
                    DialogProvider.showEditTextDialog(this, dialogTitle, dialogInputHint, toolbar.getTitle().toString(),
                            (dialog, which, text) -> toolbar.setTitle(text), null);
                    return true;
                case R.id.save:
                    presenter.onSavePressed();
                    return true;
            }
            return false;
        });

        fab.setOnClickListener((view) -> presenter.onAddPressed());
    }

    /* Contract */
    // ------------------------------------------
    @Override
    public void addKeyword(Keyword keyword) {
        keywordsFlowLayout.addKeyword(keyword);
    }

    @Override
    public void clearInputKeyword() {
        inputEditText.setText("");
    }

    @Override
    public String getInputKeyword() {
        return inputEditText.getText().toString();
    }
}
