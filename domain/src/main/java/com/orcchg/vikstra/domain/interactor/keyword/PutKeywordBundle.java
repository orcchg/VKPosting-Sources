package com.orcchg.vikstra.domain.interactor.keyword;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.UseCase;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;

import java.util.Collection;

import javax.inject.Inject;

public class PutKeywordBundle extends UseCase<Boolean> {

    public static class Parameters {
        String title;
        Collection<Keyword> keywords;

        Parameters(Builder builder) {
            this.title = builder.title;
            this.keywords = builder.keywords;
        }

        public static class Builder {
            String title;
            Collection<Keyword> keywords;

            public Builder setTitle(String title) {
                this.title = title;
                return this;
            }

            public Builder setKeywords(Collection<Keyword> keywords) {
                this.keywords = keywords;
                return this;
            }

            public Parameters build() {
                return new Parameters(this);
            }
        }
    }

    final IKeywordRepository keywordRepository;
    Parameters parameters;

    @Inject
    public PutKeywordBundle(IKeywordRepository keywordRepository,
                            ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.keywordRepository = keywordRepository;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected Boolean doAction() {
        return keywordRepository.addKeywords(parameters.title, parameters.keywords);
    }
}
