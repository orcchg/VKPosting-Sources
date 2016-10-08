package com.orcchg.vikstra.app.ui.details;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.app.ui.viewobject.ArtistDetailsVO;

interface DetailsContract {
    interface View extends MvpView {
        void setGrade(int grade);
        void showArtist(ArtistDetailsVO artist);
        void showError();
    }

    interface Presenter extends MvpPresenter<View> {
    }
}
