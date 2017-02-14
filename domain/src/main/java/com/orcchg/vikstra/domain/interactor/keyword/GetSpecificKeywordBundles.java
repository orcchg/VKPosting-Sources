package com.orcchg.vikstra.domain.interactor.keyword;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.common.IdsParameters;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;

import java.util.List;

import javax.inject.Inject;

public class GetSpecificKeywordBundles extends UseCase<List<KeywordBundle>> {

    public static class Parameters extends IdsParameters {
        public Parameters(long... ids) {
            super(ids);
        }
    }

    private final IKeywordRepository keywordRepository;
    private Parameters parameters;

    @Inject
    GetSpecificKeywordBundles(IKeywordRepository keywordRepository, ThreadExecutor threadExecutor,
                              PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.keywordRepository = keywordRepository;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected List<KeywordBundle> doAction() {
        if (parameters == null) throw new NoParametersException();
        return keywordRepository.keywords(parameters.ids);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }
}
