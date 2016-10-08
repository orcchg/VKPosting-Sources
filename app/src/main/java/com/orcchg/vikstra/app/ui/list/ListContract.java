package com.orcchg.vikstra.app.ui.list;

import android.support.v7.widget.RecyclerView;

import com.orcchg.vikstra.app.ui.base.IActivityProvider;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.app.ui.viewobject.ArtistListItemVO;

import java.util.List;

public interface ListContract {
    interface View extends MvpView, IActivityProvider {
        RecyclerView getListView();
        void showArtists(List<ArtistListItemVO> artists);
        void showError();
        void showLoading();
    }

    interface Presenter extends MvpPresenter<View> {
        void retry();
        void onScroll(int itemsLeftToEnd);
        void setGenres(List<String> genres);
    }
}
