package com.orcchg.vikstra.domain.interactor.group;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.common.ListParameters;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.repository.IGroupRepository;

import java.util.List;

import javax.inject.Inject;

public class GetGroupBundles extends UseCase<List<GroupBundle>> {

    public static class Parameters extends ListParameters {
        protected Parameters(Builder builder) {
            super(builder);
        }

        public static class Builder extends ListParameters.Builder<Builder> {
            public Parameters build() {
                return new Parameters(this);
            }
        }
    }

    private final IGroupRepository groupRepository;
    private Parameters parameters;

    @Inject
    GetGroupBundles(IGroupRepository groupRepository, ThreadExecutor threadExecutor,
                    PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.groupRepository = groupRepository;
        this.parameters = new Parameters.Builder().build();
    }

    @Nullable @Override
    protected List<GroupBundle> doAction() {
        int limit = parameters.limit();
        int offset = parameters.offset();
        return groupRepository.groups(limit, offset);
    }
}
