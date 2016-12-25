package com.orcchg.vikstra.app.ui.keyword.list;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;

public interface KeywordListContract {
    interface View extends SubView {
        void openKeywordCreateScreen(long keywordBundleId);
        void openGroupListScreen(long keywordBundleId, long postId);
        void setCloseViewResult(int result);
    }

    interface SubView extends MvpListView {
        void showKeywords(boolean isEmpty);
        void showEmptyList();
        void showError();
        void showLoading();
    }

    interface Presenter extends MvpPresenter<View> {
        void onScroll(int itemsLeftToEnd);
        void retry();
    }
}
