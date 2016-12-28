package com.orcchg.vikstra.app.ui.keyword.list;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.common.screen.LceView;
import com.orcchg.vikstra.app.ui.common.screen.ListPresenter;

public interface KeywordListContract {
    interface View extends SubView {
        void openKeywordCreateScreen(long keywordBundleId);
        void openGroupListScreen(long keywordBundleId, long postId);
        void setCloseViewResult(int result);
    }

    interface SubView extends LceView, MvpListView {
        void showKeywords(boolean isEmpty);
    }

    interface Presenter extends MvpPresenter<View>, ListPresenter {
    }
}
