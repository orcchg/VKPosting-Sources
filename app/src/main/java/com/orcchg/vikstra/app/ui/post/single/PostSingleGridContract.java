package com.orcchg.vikstra.app.ui.post.single;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;

public interface PostSingleGridContract {
    interface View extends MvpListView {
        void openNewPostScreen();
        void openPostViewScreen();
    }

    interface Presenter extends MvpPresenter<View> {
    }
}
