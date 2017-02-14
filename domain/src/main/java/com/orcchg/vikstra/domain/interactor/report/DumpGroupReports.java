package com.orcchg.vikstra.domain.interactor.report;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.repository.IReportRepository;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.file.FileUtility;
import com.orcchg.vikstra.domain.util.file.ReportComposer;

import java.util.Collection;

import javax.inject.Inject;

public class DumpGroupReports extends UseCase<String> {

    public static class Parameters implements IParameters {
        private final long groupReportBundleId;  // has priority over collection of Report-s
        private Collection<GroupReport> reports;

        public Parameters(long groupReportBundleId) {
            this.groupReportBundleId = groupReportBundleId;
        }

        public Parameters(Collection<GroupReport> reports) {
            this.groupReportBundleId = Constant.BAD_ID;
            this.reports = reports;
        }
    }

    protected String path;
    protected final ReportComposer reportComposer;
    protected final IReportRepository reportRepository;
    protected Parameters parameters;

    @Inject
    public DumpGroupReports(String path, ReportComposer reportComposer, IReportRepository reportRepository,
                            ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.path = path;
        this.reportComposer = reportComposer;
        this.reportRepository = reportRepository;
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
        if (parameters.groupReportBundleId != Constant.BAD_ID) {
            GetGroupReportBundleById useCase = new GetGroupReportBundleById(parameters.groupReportBundleId, reportRepository);
            GroupReportBundle bundle = useCase.doAction();
            if (bundle != null) parameters.reports = bundle.groupReports();
        }
        FileUtility.createFileByPath(path);  // create file or throw IOException
        return reportComposer.writeGroupReportsToCsv(parameters.reports, path) ? path : null;
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }
}
