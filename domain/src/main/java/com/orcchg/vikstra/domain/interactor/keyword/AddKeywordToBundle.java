package com.orcchg.vikstra.domain.interactor.keyword;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;
import com.orcchg.vikstra.domain.util.Constant;

import javax.inject.Inject;

public class AddKeywordToBundle extends UseCase<Boolean> {

    public static class Parameters implements IParameters {
        Keyword keyword;

        public Parameters(Keyword keyword) {
            this.keyword = keyword;
        }
    }

    private long id = Constant.BAD_ID;
    private final IKeywordRepository keywordRepository;
    private Parameters parameters;

    @Inject
    public AddKeywordToBundle(long id, IKeywordRepository keywordRepository,
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

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected Boolean doAction() {
        if (parameters == null) throw new NoParametersException();
        return keywordRepository.addKeywordToBundle(id, parameters.keyword);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }
}
