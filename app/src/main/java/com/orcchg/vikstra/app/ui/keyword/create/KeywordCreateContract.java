package com.orcchg.vikstra.app.ui.keyword.create;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.domain.model.Keyword;

public interface KeywordCreateContract {
    interface View extends MvpView {
        void clearInputKeyword();
        String getInputKeyword();
        void addKeyword(Keyword keyword);
    }

    interface Presenter extends MvpPresenter<View> {
        void onAddPressed();
        void onSavePressed();
    }
}
