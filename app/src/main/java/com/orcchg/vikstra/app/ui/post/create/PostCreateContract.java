package com.orcchg.vikstra.app.ui.post.create;

import android.content.ContentResolver;
import android.graphics.Bitmap;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;

public interface PostCreateContract {
    interface View extends MvpView {
        void addMediaThumbnail(Bitmap bmp);
        void addMediaThumbnail(String filePath);

        void clearInputText();
        String getInputText();
        void setInputText(String text);

        ContentResolver contentResolver();
        void closeView(int resultCode);
    }

    interface Presenter extends MvpPresenter<View> {
        void onAttachPressed();
        void onLocationPressed();
        void onPollPressed();
        void onSavePressed();

        void removeAttachedMedia();
    }
}
