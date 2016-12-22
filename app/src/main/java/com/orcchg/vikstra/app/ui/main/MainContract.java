package com.orcchg.vikstra.app.ui.main;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListContract;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridContract;
import com.orcchg.vikstra.app.ui.util.FabHolder;

public interface MainContract {
    interface View extends FabHolder, KeywordListContract.View, PostSingleGridContract.View {
        void openGroupListScreen(long keywordBundleId, long postId);
    }

    interface Presenter extends MvpPresenter<View> {
        void retryKeywords();
        void retryPosts();
        void onFabClick();
        void onScrollKeywordsList(int itemsLeftToEnd);
    }
}
