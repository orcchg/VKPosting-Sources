package com.orcchg.vikstra.app.ui.keyword.create;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class KeywordCreateActivity extends BaseActivity<KeywordCreateContract.View, KeywordCreateContract.Presenter>
        implements KeywordCreateContract.View {
    private static final String BUNDLE_KEY_KEYWORD_BUNDLE_ID = "bundle_key_keyword_bundle_id";
    private static final String EXTRA_KEYWORD_BUNDLE_ID = "extra_keyword_bundle_id";
    public static final int REQUEST_CODE = Constant.RequestCode.KEYWORD_CREATE_SCREEN;
    public static final int RV_TAG = Constant.ListTag.KEYWORD_CREATE_SCREEN;

    private String DIALOG_TITLE, DIALOG_HINT, SNACKBAR_KEYWORD_ALREADY_ADDED, SNACKBAR_KEYWORDS_LIMIT;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.container) ViewGroup container;
    @BindView(R.id.flow) KeywordsFlowLayout keywordsFlowLayout;
    @BindView(R.id.et_keyword_input) AutoCompleteTextView inputEditText;
    @BindView(R.id.loading_view) View loadingView;
    @BindView(R.id.error_view) View errorView;
    @OnClick(R.id.fab)
    void onFabClick() {
        presenter.onAddPressed();
    }
    @OnClick(R.id.btn_retry)
    void onRetryClick() {
        presenter.retry();
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
        initData(savedInstanceState);  // init data needed for injected dependencies
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(BUNDLE_KEY_KEYWORD_BUNDLE_ID, keywordBundleId);
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    @DebugLog
    private void initData(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            keywordBundleId = savedInstanceState.getLong(BUNDLE_KEY_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
        } else {
            keywordBundleId = getIntent().getLongExtra(EXTRA_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
        }
        Timber.d("KeywordBundle id: %s", keywordBundleId);
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
        toolbar.setNavigationOnClickListener((view) -> presenter.onBackPressed());
        toolbar.inflateMenu(R.menu.edit_save);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.edit:
                    openEditTitleDialog(toolbar.getTitle().toString(), false);
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
    public void alreadyAddedKeyword(Keyword keyword) {
        UiUtility.showSnackbar(this, String.format(Locale.ENGLISH, SNACKBAR_KEYWORD_ALREADY_ADDED, keyword.keyword()));
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
        UiUtility.showSnackbar(this, String.format(Locale.ENGLISH, SNACKBAR_KEYWORDS_LIMIT, limit));
    }

    @Override
    public void onNoKeywordsAdded() {
        UiUtility.showSnackbar(this, R.string.keyword_create_snackbar_no_keywords_added_message);
    }

    // ------------------------------------------
    @Override
    public void openEditTitleDialog(@Nullable String initTitle, boolean saveAfter) {
        DialogProvider.showEditTextDialog(this, DIALOG_TITLE, DIALOG_HINT, initTitle,
                (dialog, which, text) -> {
                    dialog.dismiss();
                    toolbar.setTitle(text);
                    presenter.onTitleChanged(text);
                    if (saveAfter) presenter.onSavePressed();
                });
    }

    @Override
    public void openSaveChangesDialog() {
        DialogProvider.showTextDialogTwoButtons(this, R.string.keyword_create_dialog_save_changes_title,
                R.string.keyword_create_dialog_save_changes_description, R.string.button_save, R.string.button_close,
                (dialog, which) -> {
                    dialog.dismiss();
                    presenter.onSavePressed();
                },
                (dialog, which) -> {
                    dialog.dismiss();
                    closeView(Activity.RESULT_CANCELED);
                });
    }

    @Override
    public void closeView() {
        finish();  // with currently set result
    }

    @Override
    public void closeView(int resultCode) {
        setResult(resultCode);
        finish();
    }

    // ------------------------------------------
    @Override
    public void showCreateKeywordBundleFailure() {
        UiUtility.showSnackbar(this, R.string.keyword_create_snackbar_failed_to_create_post,
                Snackbar.LENGTH_LONG, R.string.button_retry, (view) -> presenter.retryCreateKeywordBundle());
    }

    @Override
    public void showUpdateKeywordBundleFailure() {
        UiUtility.showSnackbar(this, R.string.keyword_create_snackbar_failed_to_update_post,
                Snackbar.LENGTH_LONG, R.string.button_retry, (view) -> presenter.retryUpdateKeywordBundle());
    }

    // ------------------------------------------
    @Override
    public boolean isContentViewVisible(int tag) {
        return true;  // always visible
    }

    @Override
    public void showContent(int tag, boolean isEmpty) {
        container.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }

    @Override
    public void showEmptyList(int tag) {
        showContent(tag, true);
    }

    @Override
    public void showError(int tag) {
        container.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoading(int tag) {
        container.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }

    /* Resources */
    // --------------------------------------------------------------------------------------------
    private void initResources() {
        Resources resources = getResources();
        DIALOG_TITLE = resources.getString(R.string.dialog_input_edit_title);
        DIALOG_HINT = resources.getString(R.string.dialog_input_edit_title_hint);
        SNACKBAR_KEYWORD_ALREADY_ADDED = resources.getString(R.string.keyword_create_snackbar_keyword_already_added_message);
        SNACKBAR_KEYWORDS_LIMIT = resources.getString(R.string.keyword_create_snackbar_keywords_limit_message);
    }
}
