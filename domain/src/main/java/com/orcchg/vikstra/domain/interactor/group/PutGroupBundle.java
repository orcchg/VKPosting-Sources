package com.orcchg.vikstra.domain.interactor.group;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.repository.IGroupRepository;

import java.util.Collection;

import javax.inject.Inject;

public class PutGroupBundle extends UseCase<Long> {

    public static class Parameters {
        String title;
        Collection<Group> groups;
        long keywordBundleId;

        Parameters(Builder builder) {
            this.title = builder.title;
            this.groups = builder.groups;
            this.keywordBundleId = builder.keywordBundleId;
        }

        public static class Builder {
            String title;
            Collection<Group> groups;
            long keywordBundleId;

            public Builder setTitle(String title) {
                this.title = title;
                return this;
            }

            public Builder setGroups(Collection<Group> groups) {
                this.groups = groups;
                return this;
            }

            public Builder setKeywordBundleId(long keywordBundleId) {
                this.keywordBundleId = keywordBundleId;
                return this;
            }

            public Parameters build() {
                return new Parameters(this);
            }
        }
    }

    final IGroupRepository groupRepository;
    Parameters parameters;

    @Inject
    public PutGroupBundle(IGroupRepository groupRepository, ThreadExecutor threadExecutor,
                          PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.groupRepository = groupRepository;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected Long doAction() {
        if (parameters == null) throw new NoParametersException();
        return groupRepository.addGroups(parameters.title, parameters.keywordBundleId, parameters.groups);
    }
}