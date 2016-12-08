package com.orcchg.vikstra.app.ui.legacy.details;

import com.orcchg.vikstra.domain.interactor.legacy.GetArtistDetails;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Artist;
import com.orcchg.vikstra.domain.util.ArtistUtils;
import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.viewobject.ArtistDetailsVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.ArtistDetailsMapper;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class DetailsPresenter extends BasePresenter<DetailsContract.View> implements DetailsContract.Presenter {

    private final GetArtistDetails getArtistDetailsUseCase;

    @Inject
    DetailsPresenter(GetArtistDetails getArtistDetailsUseCase) {
        this.getArtistDetailsUseCase = getArtistDetailsUseCase;
        this.getArtistDetailsUseCase.setPostExecuteCallback(createGetDetailsCallback());
    }

    @DebugLog @Override
    public void onStart() {
        super.onStart();
        getArtistDetailsUseCase.execute();
    }

    @Override
    protected void freshStart() {
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Artist> createGetDetailsCallback() {
        return new UseCase.OnPostExecuteCallback<Artist>() {
            @Override
            public void onFinish(Artist artist) {
                ArtistDetailsMapper mapper = new ArtistDetailsMapper();
                ArtistDetailsVO artistVO = mapper.map(artist);
                int grade = ArtistUtils.calculateGrade(artist);
                if (isViewAttached()) {
                    getView().showArtist(artistVO);
                    getView().setGrade(grade);
                }
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError();
            }
        };
    }
}
