package com.orcchg.vikstra.app.ui.post.create;

import android.content.ContentResolver;
import android.graphics.Bitmap;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.app.ui.common.screen.LceView;

public interface PostCreateContract {
    interface View extends LceView, MvpView {
        void addMediaThumbnail(Bitmap bmp);
        void addMediaThumbnail(String filePath);
        void onMediaAttachLimitReached(int limit);

        void openEditLinkDialog();
        void openMediaLoadDialog();
        void openSaveChangesDialog();

        int getThumbnailWidth();   // pixel size
        int getThumbnailHeight();  // pixel size

        void clearInputText();
        String getInputText();
        void setInputText(String text);

        void showLink(String link);

        ContentResolver contentResolver();
        void closeView();  // with currently set result
        void closeView(int resultCode, long postId);

        void showCreatePostFailure();
        void showUpdatePostFailure();
    }

    interface Presenter extends MvpPresenter<View> {
        void attachLink(String link);

        void onAttachPressed();
        void onBackPressed();
        void onLinkPressed();
        void onLocationPressed();
        void onMediaPressed();
        void onPollPressed();
        void onSavePressed();

        void removeAttachedMedia(int position);
        void retry();
        void retryCreatePost();
        void retryUpdatePost();
    }
}
