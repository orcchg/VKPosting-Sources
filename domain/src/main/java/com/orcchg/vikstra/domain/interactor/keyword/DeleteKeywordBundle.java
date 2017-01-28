package com.orcchg.vikstra.domain.interactor.keyword;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.common.IdParameters;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;
import com.orcchg.vikstra.domain.util.Constant;

import javax.inject.Inject;

public class DeleteKeywordBundle extends UseCase<Boolean> {

    private long id = Constant.BAD_ID;
    private final IKeywordRepository keywordRepository;

    @Inject
    public DeleteKeywordBundle(IKeywordRepository keywordRepository,
                               ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.keywordRepository = keywordRepository;
    }

    public void setKeywordBundleId(long id) {
        this.id = id;
    }

    public long getKeywordBundleId() {
        return id;
    }

    @Nullable @Override
    protected Boolean doAction() {
        return keywordRepository.deleteKeywords(id);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return new IdParameters(id);
    }
}
