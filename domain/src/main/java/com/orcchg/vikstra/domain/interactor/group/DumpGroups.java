package com.orcchg.vikstra.domain.interactor.group;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.repository.IGroupRepository;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.file.ReportComposer;

import java.util.Collection;

import javax.inject.Inject;

public class DumpGroups extends UseCase<String> {

    public static class Parameters {
        private long groupBundleId;  // has priority over collection of Group-s
        private Collection<Group> groups;

        public Parameters(long groupBundleId) {
            this.groupBundleId = groupBundleId;
        }

        public Parameters(Collection<Group> groups) {
            this.groups = groups;
        }
    }

    private String path;
    private final ReportComposer reportComposer;
    private final IGroupRepository groupRepository;
    private Parameters parameters;

    @Inject
    public DumpGroups(String path, ReportComposer reportComposer, IGroupRepository groupRepository,
                      ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.path = path;
        this.reportComposer = reportComposer;
        this.groupRepository = groupRepository;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Nullable @Override
    protected String doAction() {
        if (parameters == null) throw new NoParametersException();
        if (parameters.groupBundleId != Constant.BAD_ID) {
            GetGroupBundleById useCase = new GetGroupBundleById(parameters.groupBundleId, groupRepository);
            GroupBundle bundle = useCase.doAction();
            if (bundle != null) {
                parameters.groups = bundle.groups();
            }
        }
        return reportComposer.writeGroupsToCsv(parameters.groups, path) ? path : null;
    }
}
