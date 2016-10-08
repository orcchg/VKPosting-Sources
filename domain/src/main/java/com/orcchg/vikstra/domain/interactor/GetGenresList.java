package com.orcchg.vikstra.domain.interactor;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.Genre;
import com.orcchg.vikstra.domain.repository.IGenreRepository;

import java.util.List;

import javax.inject.Inject;

public class GetGenresList extends UseCase<List<Genre>> {

    final IGenreRepository genresRepository;

    @Inject
    GetGenresList(IGenreRepository genresRepository, ThreadExecutor threadExecutor,
                  PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.genresRepository = genresRepository;
    }

    @Override
    protected List<Genre> doAction() {
        return genresRepository.genres();
    }
}
