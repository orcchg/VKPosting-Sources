package com.orcchg.vikstra.domain.interactor.legacy;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.repository.IArtistRepository;

import javax.inject.Inject;

public class InvalidateArtistCache extends UseCase<Boolean> {

    final IArtistRepository artistRepository;

    @Inject
    InvalidateArtistCache(IArtistRepository artistRepository, ThreadExecutor threadExecutor,
                          PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.artistRepository = artistRepository;
    }

    @Override
    protected Boolean doAction() {
        return artistRepository.clear();
    }
}
