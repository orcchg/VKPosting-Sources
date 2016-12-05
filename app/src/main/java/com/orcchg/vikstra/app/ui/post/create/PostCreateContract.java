package com.orcchg.vikstra.app.ui.post.create;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;

public interface PostCreateContract {
    interface View extends MvpView {
    }

    interface Presenter extends MvpPresenter<View> {
    }
}
