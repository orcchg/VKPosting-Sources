package com.orcchg.vikstra.domain.interactor.legacy;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.TotalValue;
import com.orcchg.vikstra.domain.repository.IGenreRepository;

import javax.inject.Inject;

public class GetTotalGenres extends UseCase<TotalValue> {

    final IGenreRepository genreRepository;

    @Inject
    GetTotalGenres(IGenreRepository genreRepository, ThreadExecutor threadExecutor,
                   PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.genreRepository = genreRepository;
    }

    @Override
    protected TotalValue doAction() {
        return genreRepository.total();
    }
}
