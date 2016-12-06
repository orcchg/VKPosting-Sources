package com.orcchg.vikstra.app.ui.keyword.create;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.domain.interactor.GetKeywordBundleById;
import com.orcchg.vikstra.domain.interactor.UseCase;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class KeywordCreatePresenter extends BasePresenter<KeywordCreateContract.View> implements KeywordCreateContract.Presenter {

    private final GetKeywordBundleById getKeywordBundleByIdUseCase;

    @Inject
    KeywordCreatePresenter(GetKeywordBundleById getKeywordBundleByIdUseCase) {
        this.getKeywordBundleByIdUseCase = getKeywordBundleByIdUseCase;
        this.getKeywordBundleByIdUseCase.setPostExecuteCallback(createGetKeywordBundleByIdCallback());
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
    public void onAddPressed() {
        if (isViewAttached()) {
            Keyword keyword = Keyword.create(getView().getInputKeyword());
            getView().addKeyword(keyword);
            getView().clearInputKeyword();
        }
    }

    @Override
    public void onSavePressed() {
        // TODO
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
            public void onFinish(KeywordBundle keywords) {
                if (isViewAttached()) getView().setInputKeywords(keywords);
            }

            @Override
            public void onError(Throwable e) {
                // TODO: impl
            }
        };
    }
}
