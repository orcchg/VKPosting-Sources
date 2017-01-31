package com.orcchg.vikstra.app.ui.post.list;

import com.orcchg.vikstra.app.ui.post.single.PostSingleGridContract;

public interface PostListContract {
    interface View extends PostSingleGridContract.View {
        void closeView();  // with currently set result
        void closeView(int resultCode, long postId);
    }

    interface Presenter extends PostSingleGridContract.Presenter {
        void onSelectPressed();
    }
}
