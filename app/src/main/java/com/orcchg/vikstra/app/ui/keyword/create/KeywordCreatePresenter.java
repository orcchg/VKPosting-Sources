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
    private @Nullable String title;

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
        Timber.i("onAddPressed");
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
        Timber.i("onKeywordPressed: %s", keyword.toString());
        keywords.remove(keyword);
    }

    @Override
    public void onSavePressed() {
        Timber.i("onSavePressed");
        long keywordBundleId = getKeywordBundleByIdUseCase.getKeywordBundleId();
        if (TextUtils.isEmpty(title)) {
            if (isViewAttached()) getView().openEditTitleDialog(title);
        } else if (keywordBundleId == Constant.BAD_ID) {
            Timber.d("Input KeywordBundle id is BAD - add new KeywordBundle instance to repository");
            PutKeywordBundle.Parameters parameters = new PutKeywordBundle.Parameters.Builder()
                    .setTitle(title)
                    .setKeywords(keywords)  // use unordered collection
                    .build();
            putKeywordBundleUseCase.setParameters(parameters);
            putKeywordBundleUseCase.execute();
        } else {
            Timber.d("Input KeywordBundle id is [%s] - update already existing KeywordBundle instance in repository", keywordBundleId);
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
        Timber.i("onTitleChanged: %s", text);
        title = text;
    }

    // ------------------------------------------
    @Override
    public void retry() {
        Timber.i("retry");
        freshStart();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        if (isViewAttached()) getView().showLoading(KeywordCreateActivity.RV_TAG);
        getKeywordBundleByIdUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<KeywordBundle> createGetKeywordBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<KeywordBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable KeywordBundle bundle) {
                long keywordBundleId = getKeywordBundleByIdUseCase.getKeywordBundleId();
                if (keywordBundleId != Constant.BAD_ID && bundle == null) {
                    Timber.wtf("KeywordBundle wasn't found by id: %s", keywordBundleId);
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get KeywordBundle by id");
                if (bundle != null) {
                    Timber.d("Existing KeywordBundle with id [%s] will be updated on KeywordCreateScreen", keywordBundleId);
                    timestamp = bundle.timestamp();
                    title = bundle.title();
                    keywords.addAll(bundle.keywords());
                    if (isViewAttached()) {
                        getView().showContent(KeywordCreateActivity.RV_TAG, false);
                        getView().setInputKeywords(title, keywords);
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
                    Timber.wtf("Failed to update KeywordBundle in repository - item not found by correct id, as expected");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to post KeywordBundle");
                if (isViewAttached()) {
                    getView().notifyKeywordsUpdated();
                    getView().closeView(Activity.RESULT_OK);
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to post KeywordBundle");
                if (isViewAttached()) getView().showError(KeywordCreateActivity.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<KeywordBundle> createPutKeywordBundleCallback() {
        return new UseCase.OnPostExecuteCallback<KeywordBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable KeywordBundle bundle) {
                if (bundle == null) {
                    Timber.wtf("Failed to put new KeywordBundle to repository - item not created, as expected");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to put KeywordBundle");
                if (isViewAttached()) {
                    getView().notifyKeywordsAdded();
                    getView().closeView(Activity.RESULT_OK);
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to put KeywordBundle");
                if (isViewAttached()) getView().showError(KeywordCreateActivity.RV_TAG);
            }
        };
    }
}
