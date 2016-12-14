package com.orcchg.vikstra.app.ui.post.view;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.app.ui.viewobject.PostViewVO;

public interface PostViewContract {
    interface View extends MvpView {
        void showPost(PostViewVO viewObject);
    }

    interface Presenter extends MvpPresenter<View> {
    }
}
