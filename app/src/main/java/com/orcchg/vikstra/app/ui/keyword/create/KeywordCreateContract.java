package com.orcchg.vikstra.app.ui.keyword.create;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;

public interface KeywordCreateContract {
    interface View extends MvpView {
        void addKeyword(Keyword keyword);
        void clearInputKeyword();
        String getInputKeyword();
        void setInputKeywords(KeywordBundle keywords);
    }

    interface Presenter extends MvpPresenter<View> {
        void onAddPressed();
        void onSavePressed();
    }
}
