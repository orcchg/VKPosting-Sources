package com.orcchg.vikstra.app.ui.keyword.create;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.domain.model.Keyword;

import java.util.Collection;

public interface KeywordCreateContract {
    interface View extends MvpView {
        void addKeyword(Keyword keyword);
        void clearInputKeyword();
        String getInputKeyword();
        void setInputKeywords(String title, Collection<Keyword> keywords);
        void notifyKeywordsAdded();
        void notifyKeywordsUpdated();
        void onKeywordsLimitReached(int limit);

        void openEditTitleDialog(@Nullable String initTitle);
        void closeView(int resultCode);
    }

    interface Presenter extends MvpPresenter<View> {
        void onKeywordPressed(Keyword keyword);
        void onAddPressed();
        void onSavePressed();
        void onTitleChanged(String text);
    }
}
