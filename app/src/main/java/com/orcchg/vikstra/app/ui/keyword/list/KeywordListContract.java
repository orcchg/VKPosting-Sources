package com.orcchg.vikstra.app.ui.keyword.list;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;

import java.util.List;

public interface KeywordListContract {
    interface View extends MvpListView {
        void showKeywords(List<KeywordListItemVO> keywords);
        void showError();
        void showLoading();
    }

    interface Presenter extends MvpPresenter<View> {
        void retry();
        void onScroll(int itemsLeftToEnd);
    }
}
