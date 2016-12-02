package com.orcchg.vikstra.app.ui.post.single;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;

public interface PostSingleGridContract {
    interface View extends MvpListView {
    }

    interface Presenter extends MvpPresenter<View> {
    }
}
