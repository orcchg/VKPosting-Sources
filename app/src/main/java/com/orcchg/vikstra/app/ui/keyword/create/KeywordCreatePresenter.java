package com.orcchg.vikstra.app.ui.keyword.create;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.domain.model.Keyword;

import javax.inject.Inject;

public class KeywordCreatePresenter extends BasePresenter<KeywordCreateContract.View> implements KeywordCreateContract.Presenter {

    @Inject
    KeywordCreatePresenter() {
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
}
