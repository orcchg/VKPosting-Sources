package com.orcchg.vikstra.domain.interactor.keyword;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;

import javax.inject.Inject;

public class PostKeywordBundle extends UseCase<Boolean> {

    public static class Parameters {
        KeywordBundle keywords;

        public Parameters(KeywordBundle keywords) {
            this.keywords = keywords;
        }
    }

    final IKeywordRepository keywordRepository;
    Parameters parameters;

    @Inject
    public PostKeywordBundle(IKeywordRepository keywordRepository, ThreadExecutor threadExecutor,
                             PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.keywordRepository = keywordRepository;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected Boolean doAction() {
        if (parameters == null) throw new NoParametersException();
        return keywordRepository.updateKeywords(parameters.keywords);
    }
}
