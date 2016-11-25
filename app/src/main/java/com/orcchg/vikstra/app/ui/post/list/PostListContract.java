package com.orcchg.vikstra.app.ui.post.list;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;

public interface PostListContract {
    interface View extends MvpView {
    }

    interface Presenter extends MvpPresenter<View> {
    }
}
