package com.orcchg.vikstra.app.ui.keyword.create;

import android.app.Activity;
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
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class KeywordCreatePresenter extends BasePresenter<KeywordCreateContract.View> implements KeywordCreateContract.Presenter {

    private final GetKeywordBundleById getKeywordBundleByIdUseCase;
    private final PostKeywordBundle postKeywordBundleUseCase;
    private final PutKeywordBundle putKeywordBundleUseCase;

    private Set<Keyword> keywords = new TreeSet<>();
    private long timestamp;
    private String title;

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

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onAddPressed() {
        if (keywords.size() < Constant.KEYWORDS_LIMIT) {
            Keyword keyword = Keyword.create(getView().getInputKeyword());
            keywords.add(keyword);
            if (isViewAttached()) {
                getView().addKeyword(keyword);
                getView().clearInputKeyword();
            }
        } else if (isViewAttached()) {
            getView().onKeywordsLimitReached(Constant.KEYWORDS_LIMIT);
        }
    }

    @Override
    public void onKeywordPressed(Keyword keyword) {
        keywords.remove(keyword);
    }

    @Override
    public void onSavePressed() {
        long keywordBundleId = getKeywordBundleByIdUseCase.getKeywordBundleId();
        if (TextUtils.isEmpty(title)) {
            if (isViewAttached()) getView().openEditTitleDialog(title);
        } else if (keywordBundleId == Constant.BAD_ID) {
            Timber.v("add new keywords bundle to repository");
            PutKeywordBundle.Parameters parameters = new PutKeywordBundle.Parameters.Builder()
                    .setTitle(title)
                    .setKeywords(keywords)  // use unordered collection
                    .build();
            putKeywordBundleUseCase.setParameters(parameters);
            putKeywordBundleUseCase.execute();
        } else {
            Timber.v("update already existing keywords bundle in repository");
            KeywordBundle keywordsBundle = KeywordBundle.builder()
                    .setId(keywordBundleId)
                    .setKeywords(new ArrayList<>(keywords))  // turn collection into ordered list
                    .setTimestamp(timestamp)
                    .setTitle(title)
                    .build();
            PostKeywordBundle.Parameters parameters = new PostKeywordBundle.Parameters(keywordsBundle);
            postKeywordBundleUseCase.setParameters(parameters);
            postKeywordBundleUseCase.execute();
        }
    }

    @Override
    public void onTitleChanged(String text) {
        title = text;
    }

    // ------------------------------------------
    @Override
    public void retry() {
        freshStart();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    protected void freshStart() {
        if (isViewAttached()) getView().showLoading(KeywordCreateActivity.RV_TAG);
        getKeywordBundleByIdUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<KeywordBundle> createGetKeywordBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<KeywordBundle>() {
            @Override
            public void onFinish(@Nullable KeywordBundle bundle) {
                long keywordBundleId = getKeywordBundleByIdUseCase.getKeywordBundleId();
                if (keywordBundleId != Constant.BAD_ID && bundle == null) {
                    Timber.e("KeywordBundle wasn't found by id: %s", getKeywordBundleByIdUseCase.getKeywordBundleId());
                    throw new ProgramException();
                }
                if (bundle != null) {
                    timestamp = bundle.timestamp();
                    title = bundle.title();
                    keywords.addAll(bundle.keywords());
                    if (isViewAttached()) {
                        getView().showContent(KeywordCreateActivity.RV_TAG, false);
                        getView().setInputKeywords(title, keywords);
                    }
                } else {
                    Timber.d("New KeywordBundle instance will be created on this screen");
                    if (isViewAttached()) getView().showEmptyList(KeywordCreateActivity.RV_TAG);
                }
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError(KeywordCreateActivity.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Boolean> createPostKeywordBundleCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @Override
            public void onFinish(@Nullable Boolean result) {
                // TODO: result false - keyword-bundle not updated
                if (isViewAttached()) {
                    getView().notifyKeywordsUpdated();
                    getView().closeView(Activity.RESULT_OK);
                }
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError(KeywordCreateActivity.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<KeywordBundle> createPutKeywordBundleCallback() {
        return new UseCase.OnPostExecuteCallback<KeywordBundle>() {
            @Override
            public void onFinish(@Nullable KeywordBundle bundle) {
                if (bundle == null) {
                    Timber.e("Failed to create new KeywordBundle and put it to Repository");
                    throw new ProgramException();
                }
                if (isViewAttached()) {
                    getView().notifyKeywordsAdded();
                    getView().closeView(Activity.RESULT_OK);
                }
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError(KeywordCreateActivity.RV_TAG);
            }
        };
    }
}
