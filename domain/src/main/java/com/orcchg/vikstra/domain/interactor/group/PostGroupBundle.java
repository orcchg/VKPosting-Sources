package com.orcchg.vikstra.domain.interactor.group;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.repository.IGroupRepository;

import javax.inject.Inject;

public class PostGroupBundle extends UseCase<Boolean> {

    public static class Parameters {
        GroupBundle groups;

        public Parameters(GroupBundle groups) {
            this.groups = groups;
        }
    }

    private final IGroupRepository groupRepository;
    private Parameters parameters;

    @Inject
    public PostGroupBundle(IGroupRepository groupRepository, ThreadExecutor threadExecutor,
                           PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.groupRepository = groupRepository;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected Boolean doAction() {
        if (parameters == null) throw new NoParametersException();
        return groupRepository.updateGroups(parameters.groups);
    }
}
