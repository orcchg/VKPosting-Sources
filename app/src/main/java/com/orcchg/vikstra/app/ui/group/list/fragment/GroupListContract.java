package com.orcchg.vikstra.app.ui.group.list.fragment;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.domain.model.Keyword;

public interface GroupListContract {
    interface View extends MvpListView {
        void openAddKeywordDialog();
        void openGroupDetailScreen(long groupId);
        void openReportScreen(long postId);
        void showError();
        void showGroups(boolean isEmpty);

        void onPostingProgress(int progress, int total);
        void onPostingProgressInfinite();
        void onPostingComplete();

        void onPhotoUploadProgress(int progress, int total);
        void onPhotoUploadProgressInfinite();
        void onPhotoUploadComplete();
    }

    // ------------------------------------------
    interface Presenter extends MvpPresenter<View>,
            FragmentMediator.Receiver, FragmentMediator.Sender {
        void addKeyword(Keyword keyword);
        void removeListItem(int position);
        void retry();
    }
}
