package com.orcchg.vikstra.app.ui.keyword.create;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundleById;
import com.orcchg.vikstra.domain.interactor.keyword.PostKeywordBundle;
import com.orcchg.vikstra.domain.interactor.keyword.PutKeywordBundle;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class KeywordCreatePresenter extends BasePresenter<KeywordCreateContract.View> implements KeywordCreateContract.Presenter {

    private final GetKeywordBundleById getKeywordBundleByIdUseCase;
    private final PostKeywordBundle postKeywordBundleUseCase;
    private final PutKeywordBundle putKeywordBundleUseCase;

    private Memento memento = new Memento();

    // --------------------------------------------------------------------------------------------
    private static final class Memento {
        private static final String BUNDLE_KEY_KEYWORDS = "bundle_key_keywords";
        private static final String BUNDLE_KEY_HAS_KEYWORD_CHANGED = "bundle_key_has_keyword_changed";
        private static final String BUNDLE_KEY_HAS_TITLE_CHANGED = "bundle_key_has_title_changed";
        private static final String BUNDLE_KEY_TIMESTAMP = "bundle_key_timestamp";
        private static final String BUNDLE_KEY_TITLE = "bundle_key_title";

        private Set<Keyword> keywords = new TreeSet<>();
        private boolean hasKeywordsChanged;
        private boolean hasTitleChanged;
        private long timestamp;
        private @Nullable String title;

        @DebugLog @SuppressWarnings("unchecked")
        private void toBundle(Bundle outState) {
            ArrayList<Keyword> copyKeywords = new ArrayList<>(keywords);
            outState.putParcelableArrayList(BUNDLE_KEY_KEYWORDS, copyKeywords);
            outState.putBoolean(BUNDLE_KEY_HAS_KEYWORD_CHANGED, hasKeywordsChanged);
            outState.putBoolean(BUNDLE_KEY_HAS_TITLE_CHANGED, hasTitleChanged);
            outState.putLong(BUNDLE_KEY_TIMESTAMP, timestamp);
            outState.putString(BUNDLE_KEY_TITLE, title);
        }

        @DebugLog
        private static Memento fromBundle(Bundle savedInstanceState) {
            Memento memento = new Memento();
            memento.keywords = new TreeSet<>();
            List<Keyword> list = savedInstanceState.getParcelableArrayList(BUNDLE_KEY_KEYWORDS);
            if (list != null) memento.keywords.addAll(list);
            memento.hasKeywordsChanged = savedInstanceState.getBoolean(BUNDLE_KEY_HAS_KEYWORD_CHANGED, false);
            memento.hasTitleChanged = savedInstanceState.getBoolean(BUNDLE_KEY_HAS_TITLE_CHANGED, false);
            memento.timestamp = savedInstanceState.getLong(BUNDLE_KEY_TIMESTAMP);
            memento.title = savedInstanceState.getString(BUNDLE_KEY_TITLE);
            return memento;
        }
    }

    // --------------------------------------------------------------------------------------------
    @Inject
    KeywordCreatePresenter(GetKeywordBundleById getKeywordBundleByIdUseCase,
                           PostKeywordBundle postKeywordBundleUseCase, PutKeywordBundle putKeywordBundle) {
        this.getKeywordBundleByIdUseCase = getKeywordBundleByIdUseCase;
        this.getKeywordBundleByIdUseCase.setPostExecuteCallback(createGetKeywordBundleByIdCallback());
        this.postKeywordBundleUseCase = postKeywordBundleUseCase;
        this.postKeywordBundleUseCase.setPostExecuteCallback(createPostKeywordBundleCallback());
        this.putKeywordBundleUseCase = putKeywordBundle;
        this.putKeywordBundleUseCase.setPostExecuteCallback(createPutKeywordBundleCallback());
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        memento.toBundle(outState);
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onAddPressed() {
        Timber.i("onAddPressed");
        if (isViewAttached()) {
            if (memento.keywords.size() < Constant.KEYWORDS_LIMIT) {
                Keyword keyword = Keyword.create(getView().getInputKeyword());
                if (memento.keywords.add(keyword)) {
                    Timber.d("Added new Keyword: %s", keyword.keyword());
                    memento.hasKeywordsChanged = true;
                    getView().addKeyword(keyword);
                    getView().clearInputKeyword();
                } else {
                    Timber.d("Keyword %s has already been added", keyword.keyword());
                    getView().alreadyAddedKeyword(keyword);
                }
            } else {
                getView().onKeywordsLimitReached(Constant.KEYWORDS_LIMIT);
            }
        } else {
            Timber.w("No View is attached");
        }
    }

    @Override
    public void onBackPressed() {
        Timber.i("onBackPressed");
        if (isViewAttached()) {
            if (hasChanges()) {
                getView().openSaveChangesDialog();
            } else {
                getView().closeView();
            }
        } else {
            Timber.w("No View is attached");
        }
    }

    @Override
    public void onKeywordPressed(Keyword keyword) {
        Timber.i("onKeywordPressed: %s", keyword.toString());
        memento.keywords.remove(keyword);
        memento.hasKeywordsChanged = true;
    }

    @Override
    public void onSavePressed() {
        Timber.i("onSavePressed");
        if (memento.keywords.isEmpty()) {
            Timber.d("No Keyword-s added - nothing to be saved");
            if (isViewAttached()) {
                getView().onNoKeywordsAdded();
            } else {
                Timber.w("No View is attached");
            }
            return;  // don't allow to create new KeywordBundle with empty Keyword-s list
        }

        long keywordBundleId = getKeywordBundleByIdUseCase.getKeywordBundleId();
        if (TextUtils.isEmpty(memento.title)) {
            if (isViewAttached()) getView().openEditTitleDialog(memento.title, true);
        } else if (keywordBundleId == Constant.BAD_ID) {
            Timber.d("Input KeywordBundle id is BAD - add new KeywordBundle instance to repository");
            PutKeywordBundle.Parameters parameters = new PutKeywordBundle.Parameters.Builder()
                    .setTitle(memento.title)
                    .setKeywords(memento.keywords)  // use unordered collection
                    .build();
            putKeywordBundleUseCase.setParameters(parameters);
            putKeywordBundleUseCase.execute();
        } else {
            Timber.d("Input KeywordBundle id is [%s] - update already existing KeywordBundle instance in repository", keywordBundleId);
            KeywordBundle keywordsBundle = KeywordBundle.builder()
                    .setId(keywordBundleId)
                    .setKeywords(new ArrayList<>(memento.keywords))  // turn collection into ordered list
                    .setTimestamp(memento.timestamp)
                    .setTitle(memento.title)
                    .build();
            PostKeywordBundle.Parameters parameters = new PostKeywordBundle.Parameters(keywordsBundle);
            postKeywordBundleUseCase.setParameters(parameters);
            postKeywordBundleUseCase.execute();
        }
    }

    @Override
    public void onTitleChanged(String text) {
        Timber.i("onTitleChanged: %s", text);
        memento.hasTitleChanged = !text.equals(memento.title);
        memento.title = text;
    }

    // ------------------------------------------
    @Override
    public void retry() {
        Timber.i("retry");
        memento.hasKeywordsChanged = false;
        memento.hasTitleChanged = false;
        memento.keywords.clear();
        memento.title = null;
        freshStart();
    }

    @Override
    public void retryCreateKeywordBundle() {
        Timber.i("retryCreateKeywordBundle");
        putKeywordBundleUseCase.execute();  // parameters have been already set at previous failed attempt
    }

    @Override
    public void retryUpdateKeywordBundle() {
        Timber.i("retryUpdateKeywordBundle");
        postKeywordBundleUseCase.execute();  // parameters have been already set at previous failed attempt
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        if (isViewAttached()) getView().showLoading(KeywordCreateActivity.RV_TAG);
        getKeywordBundleByIdUseCase.execute();
    }

    @Override
    protected void onRestoreState() {
        memento = Memento.fromBundle(savedInstanceState);
        if (isViewAttached()) {
            getView().showContent(KeywordCreateActivity.RV_TAG, false);
            getView().setInputKeywords(memento.title, memento.keywords);
        }
    }

    @DebugLog
    private boolean hasChanges() {
        return memento.hasTitleChanged || memento.hasKeywordsChanged;
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<KeywordBundle> createGetKeywordBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<KeywordBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable KeywordBundle bundle) {
                long keywordBundleId = getKeywordBundleByIdUseCase.getKeywordBundleId();
                if (keywordBundleId != Constant.BAD_ID && bundle == null) {
                    Timber.e("KeywordBundle wasn't found by id: %s", keywordBundleId);
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get KeywordBundle by id");
                if (bundle != null) {
                    Timber.d("Existing KeywordBundle with id [%s] will be updated on KeywordCreateScreen", keywordBundleId);
                    memento.keywords.addAll(bundle.keywords());
                    memento.timestamp = bundle.timestamp();
                    memento.title = bundle.title();
                    if (isViewAttached()) {
                        getView().showContent(KeywordCreateActivity.RV_TAG, false);
                        getView().setInputKeywords(memento.title, memento.keywords);
                    }
                } else {  // bundle is null and id is BAD
                    Timber.d("New KeywordBundle instance will be created on KeywordCreateScreen");
                    if (isViewAttached()) getView().showEmptyList(KeywordCreateActivity.RV_TAG);
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get KeywordBundle by id");
                if (isViewAttached()) getView().showError(KeywordCreateActivity.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Boolean> createPostKeywordBundleCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @DebugLog @Override
            public void onFinish(@Nullable Boolean result) {
                if (result == null || !result) {
                    Timber.e("Failed to update KeywordBundle in repository - item not found by correct id, as expected");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to post KeywordBundle");
                memento.hasKeywordsChanged = false;  // changes has been saved
                if (isViewAttached()) {
                    getView().notifyKeywordsUpdated();
                    getView().closeView(Activity.RESULT_OK);
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to post KeywordBundle");
                if (isViewAttached()) getView().showUpdateKeywordBundleFailure();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<KeywordBundle> createPutKeywordBundleCallback() {
        return new UseCase.OnPostExecuteCallback<KeywordBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable KeywordBundle bundle) {
                if (bundle == null) {
                    Timber.e("Failed to put new KeywordBundle to repository - item not created, as expected");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to put KeywordBundle");
                memento.hasKeywordsChanged = false;  // changes has been saved
                if (isViewAttached()) {
                    getView().notifyKeywordsAdded();
                    getView().closeView(Activity.RESULT_OK);
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to put KeywordBundle");
                if (isViewAttached()) getView().showCreateKeywordBundleFailure();
            }
        };
    }
}
