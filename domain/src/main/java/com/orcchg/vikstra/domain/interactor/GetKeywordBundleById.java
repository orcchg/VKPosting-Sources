package com.orcchg.vikstra.domain.interactor;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;

import javax.inject.Inject;

public class GetKeywordBundleById extends UseCase<KeywordBundle> {

    final long id;
    final IKeywordRepository keywordRepository;

    @Inject
    public GetKeywordBundleById(long id, IKeywordRepository keywordRepository,
                                ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.id = id;
        this.keywordRepository = keywordRepository;
    }

    @Override
    protected KeywordBundle doAction() {
        return keywordRepository.keywords(id);
    }
}
