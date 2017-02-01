package com.orcchg.vikstra.app.ui.post.single;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.common.screen.LceView;
import com.orcchg.vikstra.app.ui.common.screen.ListPresenter;

public interface PostSingleGridContract {
    interface View extends SubView {
        void openPostCreateScreen();
        void openPostViewScreen(long postId);
        void showCreatePostFailure();
    }

    interface SubView extends LceView, MvpListView {
        void showPosts(boolean isEmpty);
    }

    interface Presenter extends MvpPresenter<View>, ListPresenter {
        void removeListItem(int position);
        void retry();
    }
}
