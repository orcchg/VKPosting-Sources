package com.orcchg.vikstra.domain.interactor.group;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.repository.IGroupRepository;

import javax.inject.Inject;

public class PostGroupBundle extends UseCase<Boolean> {

    public static class Parameters implements IParameters {
        final GroupBundle groups;

        final long id;
        String title = "";  // title could be updated separately

        public Parameters(GroupBundle groups) {
            this.id = groups.id();
            this.groups = groups;
        }

        public Parameters(long id, String title) {
            this.id = id;
            this.groups = null;
            this.title = title;
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
        if (parameters.groups != null) return groupRepository.updateGroups(parameters.groups);
        return groupRepository.updateGroupsTitle(parameters.id, parameters.title);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }
}
