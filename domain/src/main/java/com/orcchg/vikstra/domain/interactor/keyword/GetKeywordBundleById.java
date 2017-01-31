package com.orcchg.vikstra.domain.interactor.keyword;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.common.IdParameters;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;

import javax.inject.Inject;

public class GetKeywordBundleById extends UseCase<KeywordBundle> {

    private long id;
    private final IKeywordRepository keywordRepository;

    @Inject
    public GetKeywordBundleById(long id, IKeywordRepository keywordRepository,
                                ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.id = id;
        this.keywordRepository = keywordRepository;
    }

    public long getKeywordBundleId() {
        return id;
    }

    public void setKeywordBundleId(long id) {
        this.id = id;
    }

    @Nullable @Override
    protected KeywordBundle doAction() {
        return keywordRepository.keywords(id);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return new IdParameters(id);
    }
}
