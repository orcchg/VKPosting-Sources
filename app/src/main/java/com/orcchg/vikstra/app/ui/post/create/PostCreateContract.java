package com.orcchg.vikstra.app.ui.post.create;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;

public interface PostCreateContract {
    interface View extends MvpView {
        void addMedia();  // TODO: specify URI
        void clearInputText();
        String getInputText();
    }

    interface Presenter extends MvpPresenter<View> {
        void onAttachPressed();
        void onLocationPressed();
        void onMediaPressed();
        void onPollPressed();
        void onSavePressed();
    }
}
