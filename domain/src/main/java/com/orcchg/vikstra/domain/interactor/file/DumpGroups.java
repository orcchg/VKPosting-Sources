package com.orcchg.vikstra.domain.interactor.file;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.util.file.ReportComposer;

import java.util.Collection;

import javax.inject.Inject;

public class DumpGroups extends UseCase<Boolean> {

    public static class Parameters {
        private Collection<Group> groups;

        public Parameters(Collection<Group> groups) {
            this.groups = groups;
        }
    }

    private final String path;
    private final ReportComposer reportComposer;
    Parameters parameters;

    @Inject
    public DumpGroups(String path, ReportComposer reportComposer, ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.path = path;
        this.reportComposer = reportComposer;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected Boolean doAction() {
        return reportComposer.writeGroupsToCsv(parameters.groups, path);
    }
}
