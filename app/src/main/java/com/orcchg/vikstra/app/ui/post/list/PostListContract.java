package com.orcchg.vikstra.app.ui.post.list;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.common.screen.ListPresenter;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridContract;

public interface PostListContract {
    interface View extends PostSingleGridContract.View {
    }

    interface SubView extends PostSingleGridContract.SubView {
    }

    interface Presenter extends MvpPresenter<View>, ListPresenter {
    }
}
