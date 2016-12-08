package com.orcchg.vikstra.domain.interactor.keyword;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.UseCase;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;

import java.util.List;

import javax.inject.Inject;

public class GetKeywordBundles extends UseCase<List<KeywordBundle>> {

    public static class Parameters {
        int limit = -1;
        int offset = 0;

        Parameters(Builder builder) {
            this.limit = builder.limit;
            this.offset = builder.offset;
        }

        public static class Builder {
            int limit = -1;
            int offset = 0;

            public Builder setLimit(int limit) {
                this.limit = limit;
                return this;
            }

            public Builder setOffset(int offset) {
                this.offset = offset;
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
    GetKeywordBundles(IKeywordRepository keywordRepository, ThreadExecutor threadExecutor,
                      PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.keywordRepository = keywordRepository;
        this.parameters = new Parameters.Builder().build();
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected List<KeywordBundle> doAction() {
        int limit = parameters.limit;
        int offset = parameters.offset;
        return keywordRepository.keywords(limit, offset);
    }
}
