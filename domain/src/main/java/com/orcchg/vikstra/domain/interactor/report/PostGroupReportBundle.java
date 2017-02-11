package com.orcchg.vikstra.domain.interactor.report;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.repository.IReportRepository;

import javax.inject.Inject;

public class PostGroupReportBundle extends UseCase<Boolean> {

    public static class Parameters implements IParameters {
        GroupReportBundle reports;

        public Parameters(GroupReportBundle reports) {
            this.reports = reports;
        }
    }

    private final IReportRepository reportRepository;
    private Parameters parameters;

    @Inject
    public PostGroupReportBundle(IReportRepository reportRepository, ThreadExecutor threadExecutor,
                                 PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.reportRepository = reportRepository;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected Boolean doAction() {
        if (parameters == null) throw new NoParametersException();
        return reportRepository.updateReports(parameters.reports);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }
}
