package com.orcchg.vikstra.app.ui.keyword.create;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.app.ui.common.view.KeywordsFlowLayout;
import com.orcchg.vikstra.app.ui.keyword.create.injection.DaggerKeywordCreateComponent;
import com.orcchg.vikstra.app.ui.keyword.create.injection.KeywordCreateComponent;
import com.orcchg.vikstra.app.ui.keyword.create.injection.KeywordCreateModule;
import com.orcchg.vikstra.app.ui.util.UiUtility;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class KeywordCreateActivity extends BaseActivity<KeywordCreateContract.View, KeywordCreateContract.Presenter>
        implements KeywordCreateContract.View {
    public static final int REQUEST_CODE = Constant.RequestCode.KEYWORD_CREATE_SCREEN;
    private static final String EXTRA_KEYWORD_BUNDLE_ID = "extra_keyword_bundle_id";

    private String DIALOG_TITLE, DIALOG_HINT, SNACKBAR_KEYWORDS_LIMIT;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.flow) KeywordsFlowLayout keywordsFlowLayout;
    @BindView(R.id.et_keyword_input) AutoCompleteTextView inputEditText;
    @OnClick(R.id.fab)
    void onFabClick() {
        presenter.onAddPressed();
    }

    private KeywordCreateComponent keywordCreateComponent;
    private long keywordBundleId = Constant.BAD_ID;

    public static Intent getCallingIntent(@NonNull Context context) {
        return getCallingIntent(context, Constant.BAD_ID);
    }

    public static Intent getCallingIntent(@NonNull Context context, long keywordBunldeId) {
        Intent intent = new Intent(context, KeywordCreateActivity.class);
        intent.putExtra(EXTRA_KEYWORD_BUNDLE_ID, keywordBunldeId);
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
                .keywordCreateModule(new KeywordCreateModule(keywordBundleId))
                .build();
        keywordCreateComponent.inject(this);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData();  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keywords_create);
        ButterKnife.bind(this);
        initResources();
        initView();
        initToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        inputEditText.requestFocus();
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    private void initData() {
        keywordBundleId = getIntent().getLongExtra(EXTRA_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        inputEditText.setOnEditorActionListener((view, actionId, keyEvent) -> {
            presenter.onAddPressed();
            return true;
        });
        keywordsFlowLayout.enableLayoutTransition(true);  // animation
        keywordsFlowLayout.setOnKeywordItemClickListener((keyword) -> presenter.onKeywordPressed(keyword));
        keywordsFlowLayout.setKeywordDeletable(true);
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.keyword_create_screen_title);
        toolbar.setNavigationOnClickListener((view) -> closeView(Activity.RESULT_CANCELED));
        toolbar.inflateMenu(R.menu.edit_save);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.edit:
                    openEditTitleDialog(toolbar.getTitle().toString());
                    return true;
                case R.id.save:
                    presenter.onSavePressed();
                    return true;
            }
            return false;
        });
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
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

    @Override
    public void setInputKeywords(String title, Collection<Keyword> keywords) {
        if (!TextUtils.isEmpty(title)) toolbar.setTitle(title);
        keywordsFlowLayout.setKeywords(keywords);
    }

    @Override
    public void notifyKeywordsAdded() {
        Toast.makeText(this, R.string.keyword_create_bundle_added, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyKeywordsUpdated() {
        Toast.makeText(this, R.string.keyword_create_bundle_updated, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onKeywordsLimitReached(int limit) {
        UiUtility.showSnackbar(this, String.format(SNACKBAR_KEYWORDS_LIMIT, limit));
    }

    // ------------------------------------------
    @Override
    public void openEditTitleDialog(@Nullable String initTitle) {
        DialogProvider.showEditTextDialog(this, DIALOG_TITLE, DIALOG_HINT, initTitle,
                (dialog, which, text) -> {
                    toolbar.setTitle(text);
                    presenter.onTitleChanged(text);
                });
    }

    @Override
    public void closeView(int resultCode) {
        setResult(resultCode);
        finish();
    }

    @Override
    public void showError() {
        // TODO: show error
    }

    /* Resources */
    // --------------------------------------------------------------------------------------------
    private void initResources() {
        Resources resources = getResources();
        DIALOG_TITLE = resources.getString(R.string.dialog_input_edit_title);
        DIALOG_HINT = resources.getString(R.string.dialog_input_edit_title_hint);
        SNACKBAR_KEYWORDS_LIMIT = resources.getString(R.string.keyword_create_snackbar_keywords_limit_message);
    }
}
