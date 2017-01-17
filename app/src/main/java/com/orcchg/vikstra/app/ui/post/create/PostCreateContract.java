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
        void openMediaLoadDialog();
        void openSaveChangesDialog();

        void clearInputText();
        String getInputText();
        void setInputText(String text);

        ContentResolver contentResolver();
        void closeView();  // with currently set result
        void closeView(int resultCode);

        void showCreatePostFailure();
        void showUpdatePostFailure();
    }

    interface Presenter extends MvpPresenter<View> {
        void onAttachPressed();
        void onBackPressed();
        void onLocationPressed();
        void onMediaPressed();
        void onPollPressed();
        void onSavePressed();

        void removeAttachedMedia(int position);
        void retry();
    }
}
