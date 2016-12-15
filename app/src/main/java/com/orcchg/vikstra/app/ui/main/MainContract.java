package com.orcchg.vikstra.app.ui.main;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListContract;

public interface MainContract {
    interface View extends KeywordListContract.View {
        void openGroupListScreen(long keywordBundleId);
        void showFab(boolean isVisible);
    }

    interface Presenter extends MvpPresenter<View> {
        void retryKeywords();
        void retryPosts();
        void onFabClick();
        void onScroll(int itemsLeftToEnd);
    }
}
