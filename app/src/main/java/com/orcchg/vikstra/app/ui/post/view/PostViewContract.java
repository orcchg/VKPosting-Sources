package com.orcchg.vikstra.app.ui.post.view;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;

public interface PostViewContract {
    interface View extends MvpView {
    }

    interface Presenter extends MvpPresenter<View> {
    }
}
