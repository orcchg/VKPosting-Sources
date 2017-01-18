package com.orcchg.vikstra.app.ui.keyword.create;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.app.ui.common.screen.LceView;
import com.orcchg.vikstra.domain.model.Keyword;

import java.util.Collection;

public interface KeywordCreateContract {
    interface View extends LceView, MvpView {
        void addKeyword(Keyword keyword);
        void clearInputKeyword();
        String getInputKeyword();
        void setInputKeywords(String title, Collection<Keyword> keywords);
        void notifyKeywordsAdded();
        void notifyKeywordsUpdated();
        void onKeywordsLimitReached(int limit);
        void onNoKeywordsAdded();

        void openEditTitleDialog(@Nullable String initTitle, boolean saveAfter);
        void openSaveChangesDialog();
        void closeView();  // with currently set result
        void closeView(int resultCode);
    }

    interface Presenter extends MvpPresenter<View> {
        void onAddPressed();
        void onBackPressed();
        void onKeywordPressed(Keyword keyword);
        void onSavePressed();
        void onTitleChanged(String text);

        void retry();
    }
}
