package com.orcchg.vikstra.domain.interactor.file;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.util.file.ReportComposer;

import java.util.Collection;

import javax.inject.Inject;

public class DumpGroupReports extends UseCase<Boolean> {

    public static class Parameters {
        private Collection<GroupReport> reports;

        public Parameters(Collection<GroupReport> reports) {
            this.reports = reports;
        }
    }

    private final String path;
    private final ReportComposer reportComposer;
    Parameters parameters;

    @Inject
    public DumpGroupReports(String path, ReportComposer reportComposer, ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.path = path;
        this.reportComposer = reportComposer;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected Boolean doAction() {
        return reportComposer.writeGroupReportsToCsv(parameters.reports, path);
    }
}
