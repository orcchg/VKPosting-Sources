package com.orcchg.vikstra.app.ui.legacy.tab;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.domain.interactor.legacy.GetGenresList;
import com.orcchg.vikstra.domain.interactor.legacy.GetTotalGenres;
import com.orcchg.vikstra.domain.interactor.legacy.InvalidateGenreCache;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Genre;
import com.orcchg.vikstra.domain.model.TotalValue;

import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class TabPresenter extends BasePresenter<TabContract.View> implements TabContract.Presenter {

    private final GetGenresList getGenresListUseCase;
    private final GetTotalGenres getTotalGenresUseCase;
    private final InvalidateGenreCache invalidateCacheUseCase;

    static class Memento {
        static final String BUNDLE_KEY_TOTAL_GENRES = "bundle_key_total_genres";

        int totalGenres = 0;

        void toBundle(Bundle outState) {
            outState.putInt(BUNDLE_KEY_TOTAL_GENRES, totalGenres);
        }

        static Memento fromBundle(Bundle savedInstanceState) {
            Memento memento = new Memento();
            memento.totalGenres = savedInstanceState.getInt(BUNDLE_KEY_TOTAL_GENRES);
            return memento;
        }
    }

    Memento memento;

    @Inject
    TabPresenter(GetGenresList getGenresListUseCase, GetTotalGenres getTotalGenresUseCase,
                 InvalidateGenreCache invalidateCacheUseCase) {
        this.getGenresListUseCase = getGenresListUseCase;
        this.getTotalGenresUseCase = getTotalGenresUseCase;
        this.invalidateCacheUseCase = invalidateCacheUseCase;
        this.getGenresListUseCase.setPostExecuteCallback(createGetGenresCallback());
        this.getTotalGenresUseCase.setPostExecuteCallback(createGetTotalCallback());
        this.invalidateCacheUseCase.setPostExecuteCallback(createInvalidateCacheCallback());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            memento = Memento.fromBundle(savedInstanceState);
        } else {
            this.memento = new Memento();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        memento.toBundle(outState);
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void retry() {
        invalidateCache();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    protected void freshStart() {
        if (isStateRestored()) {
            loadGenres();
        } else if (memento.totalGenres <= 0) {
            getTotalGenresUseCase.execute();
        }
    }

    @DebugLog
    private void loadGenres() {
        if (isViewAttached()) {
            if (memento.totalGenres <= 0) {
                getView().showLoading();
            }
        }
        getGenresListUseCase.execute();
    }

    @DebugLog
    private void invalidateCache() {
        memento.totalGenres = 0;
        if (isViewAttached()) getView().showLoading();
        invalidateCacheUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<List<Genre>> createGetGenresCallback() {
        return new UseCase.OnPostExecuteCallback<List<Genre>>() {
            @Override
            public void onFinish(List<Genre> genres) {
                if (isViewAttached()) getView().showTabs(genres);
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<TotalValue> createGetTotalCallback() {
        return new UseCase.OnPostExecuteCallback<TotalValue>() {
            @Override
            public void onFinish(TotalValue total) {
                Timber.i("Total genres: %s", total.getValue());
                memento.totalGenres = total.getValue();
                loadGenres();
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Boolean> createInvalidateCacheCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @Override
            public void onFinish(Boolean result) {
                freshStart();
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError();
            }
        };
    }
}