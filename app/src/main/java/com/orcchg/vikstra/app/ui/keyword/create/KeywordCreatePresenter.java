package com.orcchg.vikstra.app.ui.keyword.create;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.domain.interactor.UseCase;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundleById;
import com.orcchg.vikstra.domain.interactor.keyword.PutKeywordBundle;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

import static rx.schedulers.Schedulers.start;

public class KeywordCreatePresenter extends BasePresenter<KeywordCreateContract.View> implements KeywordCreateContract.Presenter {
    private static final int KEYWORDS_LIMIT = 7;

    private final GetKeywordBundleById getKeywordBundleByIdUseCase;
    private final PutKeywordBundle putKeywordBundle;

    private String title;
    private Set<Keyword> keywords = new TreeSet<>();

    @Inject
    KeywordCreatePresenter(GetKeywordBundleById getKeywordBundleByIdUseCase, PutKeywordBundle putKeywordBundle) {
        this.getKeywordBundleByIdUseCase = getKeywordBundleByIdUseCase;
        this.getKeywordBundleByIdUseCase.setPostExecuteCallback(createGetKeywordBundleByIdCallback());
        this.putKeywordBundle = putKeywordBundle;
        this.putKeywordBundle.setPostExecuteCallback(createPutKeywordBundleCallback());
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void onStart() {
        super.onStart();
        start();
    }

    /* Contract */
    // ------------------------------------------
    @Override
    public void onKeywordPressed(Keyword keyword) {
        keywords.remove(keyword);
    }

    @Override
    public void onAddPressed() {
        if (keywords.size() < KEYWORDS_LIMIT) {
            Keyword keyword = Keyword.create(getView().getInputKeyword());
            keywords.add(keyword);
            if (isViewAttached()) {
                getView().addKeyword(keyword);
                getView().clearInputKeyword();
            }
        } else if (isViewAttached()) {
            getView().onKeywordsLimitReached(KEYWORDS_LIMIT);
        }
    }

    @Override
    public void onSavePressed() {
        if (TextUtils.isEmpty(title)) {
            if (isViewAttached()) getView().openEditTitleDialog(title);
        } else {
            PutKeywordBundle.Parameters parameters = new PutKeywordBundle.Parameters.Builder()
                    .setTitle(title)
                    .setKeywords(keywords)
                    .build();
            putKeywordBundle.setParameters(parameters);
            putKeywordBundle.execute();
        }
    }

    @Override
    public void onTitleChanged(String text) {
        title = text;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog
    public void start() {
        getKeywordBundleByIdUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<KeywordBundle> createGetKeywordBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<KeywordBundle>() {
            @Override
            public void onFinish(@Nullable KeywordBundle values) {
                if (values != null) {
                    title = values.title();
                    keywords.addAll(values.keywords());
                }
                if (isViewAttached()) getView().setInputKeywords(title, keywords);
            }

            @Override
            public void onError(Throwable e) {
                // TODO: impl
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Boolean> createPutKeywordBundleCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @Override
            public void onFinish(@Nullable Boolean values) {
                if (isViewAttached()) {
                    getView().notifyKeywordsSaved();
                    getView().closeView(Activity.RESULT_OK);
                }
            }

            @Override
            public void onError(Throwable e) {
                // TODO: impl
            }
        };
    }
}
