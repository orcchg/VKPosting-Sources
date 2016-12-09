package com.orcchg.vikstra.app.ui.main;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListContract;
import com.orcchg.vikstra.app.ui.post.list.PostListContract;

public interface MainContract {
    interface View extends KeywordListContract.View, PostListContract.View {
        void openGroupListScreen(long keywordBundleId);
        void showFab(boolean isVisible);
    }

    interface Presenter extends MvpPresenter<View> {
        void retry();
        void onFabClick();
        void onScroll(int itemsLeftToEnd);
    }
}
